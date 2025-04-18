package com.kona.ktc.v0.infrastructure.redis

import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.model.TrafficWaiting
import com.kona.ktc.v0.domain.repository.TrafficRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Instant

@Repository
class TrafficRedisTemplateRepository(
    private val stringRedisTemplate: StringRedisTemplate
) : TrafficRepository {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val DEFAULT_THRESHOLD = 1000L
        private const val REFILL_PERIOD = 60L // 1분
    }

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        // Redis 키 생성
        val zqueueKey = "ktc:zqueue:${token.zoneId}"
        val tokenKey = "ktc:tokens:${token.zoneId}"
        val thresholdKey = "ktc:threshold:${token.zoneId}"
        val lastRefillKey = "ktc:last_refill_ts:${token.zoneId}"

        // 1. 사용자 대기열 등록 (중복 방지)
        stringRedisTemplate.opsForZSet().addIfAbsent(zqueueKey, token.token, score.toDouble())

        // 2. 사용자 순번 조회
        val waitingNumber = stringRedisTemplate.opsForZSet().rank(zqueueKey, token.token)
            ?: throw IllegalStateException("User not found in zqueue after insert")

        logger.info("[$token] waitingNumber : $waitingNumber")

        // 3. 현재 토큰 수, 마지막 리필 시각 조회
        val availableTokens = stringRedisTemplate.opsForValue().get(tokenKey)?.toLong() ?: 0L
        val lastRefill = stringRedisTemplate.opsForValue().get(lastRefillKey)?.toLong() ?: 0L
        logger.info("[$token] availableTokens : $availableTokens")
        logger.info("[$token] lastRefill : $lastRefill")

        // 4. 처리 속도 설정 조회
        var threshold = stringRedisTemplate.opsForValue().get(thresholdKey)?.toLong()
        logger.info("[$token] threshold - 1 : $threshold")
        if (threshold == null || threshold <= 0) {
            threshold = DEFAULT_THRESHOLD
            stringRedisTemplate.opsForValue().set(thresholdKey, threshold.toString())
        }
        logger.info("[$token] threshold - 2 : $threshold")

        // 5. 리필 필요 여부 판단 (1분 단위)
        var currentTokens = availableTokens
        if (now - lastRefill >= REFILL_PERIOD) {
            currentTokens = threshold
            stringRedisTemplate.opsForValue().set(tokenKey, currentTokens.toString())
            stringRedisTemplate.opsForValue().set(lastRefillKey, now.toString())
        }
        logger.info("[$token] currentTokens : $currentTokens")

        // 6. 진입 가능 여부 판단
        logger.info("[$token] waitingNumber : $waitingNumber / currentTokens : $currentTokens / waitingNumber < currentTokens : ${waitingNumber < currentTokens}")
        val estimatedTime = if (waitingNumber < currentTokens) {
            // 진입 가능한 경우
            stringRedisTemplate.opsForZSet().remove(zqueueKey, token.token)
            stringRedisTemplate.opsForValue().decrement(tokenKey)
            0L
        } else {
            // 대기해야 하는 경우
            // waitingNumber는 0부터 시작하므로, 실제 대기 순번은 waitingNumber + 1
            // currentTokens는 현재 사용 가능한 토큰 수
            // threshold는 분당 처리 가능한 토큰 수
            ((waitingNumber - currentTokens + 1) * 60 / threshold)
        }

        val totalCount = stringRedisTemplate.opsForZSet().size(zqueueKey) ?: 0L
        logger.info("[$token] totalCount : $totalCount")

        return TrafficWaiting(
            number = waitingNumber + 1,
            estimatedTime = estimatedTime,
            totalCount = totalCount
        )
    }
} 