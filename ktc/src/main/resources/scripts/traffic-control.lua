-- 1. 트래픽 요청 토큰 Queue 저장 (이미 존재하면 추가 안 함)
local function addQueue(queueKey, token, score)
    redis.call('ZADD', queueKey, 'NX', score, token)
end

-- 2. 분당 버킷 리필 시간 확인 및 리필 처리
local function refillMinuteBucket(minuteThresholdKey, minuteBucketKey, minuteBucketRefillTimeKey, nowMillis, defaultMinuteThreshold)
    redis.call('SETNX', minuteThresholdKey, defaultMinuteThreshold)
    local minuteThreshold = tonumber(redis.call('GET', minuteThresholdKey))

    redis.call('SETNX', minuteBucketKey, minuteThreshold)
    redis.call('SETNX', minuteBucketRefillTimeKey, nowMillis)

    local minuteBucketLastRefillTime = tonumber(redis.call('GET', minuteBucketRefillTimeKey))

    if nowMillis - minuteBucketLastRefillTime >= 60000 then
        redis.call('SET', minuteBucketKey, minuteThreshold)
        redis.call('SET', minuteBucketRefillTimeKey, nowMillis)
    end
    return minuteThreshold
end

-- 3. 초당 버킷 리필 시간 확인 및 리필 처리
local function refillSecondBucket(secondBucketKey, secondBucketRefillTimeKey, nowMillis, perSecondThreshold, applySecondBucket)
    if applySecondBucket then
        redis.call('SETNX', secondBucketKey, perSecondThreshold)
        redis.call('SETNX', secondBucketRefillTimeKey, nowMillis)

        local secondBucketLastRefillTime = tonumber(redis.call('GET', secondBucketRefillTimeKey))

        if nowMillis - secondBucketLastRefillTime >= 1000 then
            redis.call('SET', secondBucketKey, perSecondThreshold)
            redis.call('SET', secondBucketRefillTimeKey, nowMillis)
        end
    end
end

-- 4. 트래픽 진입 가능 여부 판단
local function checkEntry(queueKey, entryCountKey, token, nowMillis, minuteThreshold, secondThreshold, secondBucketKey, minuteBucketKey, applySecondBucket)
    local rank = redis.call('ZRANK', queueKey, token)

    local currentSecondBucketSize = tonumber(redis.call('GET', secondBucketKey) or secondThreshold)
    local currentMinuteBucketSize = tonumber(redis.call('GET', minuteBucketKey) or minuteThreshold)
    local canEnter = false

    if currentSecondBucketSize > 0 and currentMinuteBucketSize > 0 then
        if rank < minuteThreshold then
            canEnter = true
        else
            local score = tonumber(redis.call('ZSCORE', queueKey, token))
            local waitingTime = nowMillis - score + 1000
            local numberInQueueForCalc = rank + 1
            local effectiveMinuteThresholdForCalc = math.max(1, minuteThreshold)
            local estimatedTime = math.ceil(numberInQueueForCalc / effectiveMinuteThresholdForCalc) * 60000
            if waitingTime >= estimatedTime then
                canEnter = true
            end
        end
    end

    if canEnter then
        if applySecondBucket then redis.call('DECR', secondBucketKey) end
        redis.call('DECR', minuteBucketKey)
        redis.call('INCR', entryCountKey)
        redis.call('ZREM', queueKey, token)
        return { 1, 0, 0, 0 }
    else
        local numberInQueue = rank + 1
        local effectiveMinuteThreshold = math.max(1, minuteThreshold)
        local estimatedWaitTimeMillis = math.ceil(numberInQueue / effectiveMinuteThreshold) * 60000
        local totalCountInQueue = redis.call('ZCARD', queueKey)
        return { 0, numberInQueue, estimatedWaitTimeMillis, totalCountInQueue }
    end
end

-- KEYS (Lua 스크립트에 전달되는 키 목록)
-- KEYS[1] = queueKey                  (대기열 ZSET 키)
-- KEYS[2] = queueStatusKey            (대기열 상태 저장 키)
-- KEYS[3] = minuteThresholdKey        (분당 임계치 저장 키)
-- KEYS[4] = minuteBucketKey           (분당 사용량 버킷 키)
-- KEYS[5] = minuteBucketRefillTimeKey (분당 버킷 리필 시간 키)
-- KEYS[6] = secondBucketKey           (초당 사용량 버킷 키)
-- KEYS[7] = secondBucketRefillTimeKey (초당 버킷 리필 시간 키)
-- KEYS[8] = entryCountKey             (전체 진입 카운트 키)

local queueKey                  = KEYS[1]
local queueStatusKey            = KEYS[2]
local minuteThresholdKey        = KEYS[3]
local minuteBucketKey           = KEYS[4]
local minuteBucketRefillTimeKey = KEYS[5]
local secondBucketKey           = KEYS[6]
local secondBucketRefillTimeKey = KEYS[7]
local entryCountKey             = KEYS[8]

-- ARGV (Lua 스크립트에 전달되는 인자 목록)
-- ARGV[1] = token                (현재 요청 토큰)
-- ARGV[2] = score                (ZSET에 저장될 점수, 일반적으로 타임스탬프)
-- ARGV[3] = nowMillis            (현재 시간, 밀리초)
-- ARGV[4] = defaultThreshold     (기본 분당 임계치)

local token                   = ARGV[1]
local score                   = tonumber(ARGV[2])
local nowMillis               = tonumber(ARGV[3])
local defaultMinuteThreshold  = ARGV[4] -- defaultThreshold는 분당 임계치의 기본값

local queueStatus = redis.call('GET', queueStatusKey) or "ACTIVE"
if queueStatus == 'BLOCKED' then
    return { -1, 0, 0, 0 }
end

-- 1. 요청 토큰을 대기열에 추가 (이미 있다면 점수 업데이트 안함)
addQueue(queueKey, token, score)

-- 2. 분당 버킷 리필 및 현재 분당 임계치(minuteThreshold) 가져오기
local minuteThreshold = refillMinuteBucket(minuteThresholdKey, minuteBucketKey, minuteBucketRefillTimeKey, nowMillis, defaultMinuteThreshold)

-- 3. 초당 임계치(secondThreshold) 계산 (분당 임계치 / 60초, 올림, 최소 1 보장)
local secondThreshold = math.max(1, math.ceil(minuteThreshold / 60.0))

-- 4. 초당 버킷 리필
local queueSize = redis.call('ZCARD', queueKey)
local applySecondBucket = queueSize >= 2
refillSecondBucket(secondBucketKey, secondBucketRefillTimeKey, nowMillis, secondThreshold, applySecondBucket)

-- 5. 트래픽 진입 가능 여부 판단 및 결과 반환
return checkEntry(queueKey, entryCountKey, token, nowMillis, minuteThreshold, secondThreshold, secondBucketKey, minuteBucketKey, applySecondBucket)
