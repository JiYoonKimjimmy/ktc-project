package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ZERO
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficTokenExpireExecuteAdapter(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) {
    /**
     * [트래픽 만료 프로세스]
     * 1. 현재 트래픽 대기 Queue (현재 시간 - 6초) 이전 Score Token 전체 조회
     * 2. 각 토큰 별 last_polling_time 6초 초과 여부 확인
     *    - last_polling_time 6초 초과하는 경우, 트래픽 대기 Queue 에서 제거
     */
    suspend fun expireTraffic(now: Instant) {
        val activeZoneIds = reactiveStringRedisTemplate.opsForSet().members(ACTIVATION_ZONES.key).collectList().awaitSingle()
        val zoneKeys = activeZoneIds.map {
            val queueKey = QUEUE.getKey(it)
            val tokenLastPollingTimeKey = TOKEN_LAST_POLLING_TIME.getKey(it)
            Pair(queueKey, tokenLastPollingTimeKey)
        }

        zoneKeys.forEach { (queueKey, tokenLastPollingTimeKey) ->
            // 0 번째 부터 ~ (현재시간 - 1분)
            val expiredRange = Range.closed(ZERO.toDouble(), (now.toEpochMilli() - ONE_MINUTE_MILLIS).toDouble())
            val expiredTokens = reactiveStringRedisTemplate.opsForZSet()
                .rangeByScore(tokenLastPollingTimeKey, expiredRange)
                .collectList()
                .awaitSingle()

            if (expiredTokens.isNotEmpty()) {
                // queueKey & tokenLastPollingTimeKey ZSet 만료 대상 token 제거
                reactiveStringRedisTemplate.opsForZSet().remove(queueKey, *expiredTokens.toTypedArray()).awaitSingle()
                reactiveStringRedisTemplate.opsForZSet().remove(tokenLastPollingTimeKey, *expiredTokens.toTypedArray()).awaitSingle()
            }
        }
    }
}