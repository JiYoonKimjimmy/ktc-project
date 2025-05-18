-- 1. 트래픽 요청 토큰 Queue 저장
local function addQueue(queueKey, token, score)
    redis.call('ZADD', queueKey, 'NX', score, token)
end

-- 2. 토큰-버킷 리필 시간 확인 및 리필 처리
local function checkBucket(thresholdKey, bucketKey, bucketRefillTimeKey, nowMillis, defaultThreshold)
    redis.call('SETNX', thresholdKey, defaultThreshold)
    redis.call('SETNX', bucketKey, defaultThreshold)
    redis.call('SETNX', bucketRefillTimeKey, nowMillis)

    local threshold = tonumber(redis.call('GET', thresholdKey))
    local bucketRefillTime = tonumber(redis.call('GET', bucketRefillTimeKey))

    if tonumber(nowMillis) - bucketRefillTime >= 60000 then
        redis.call('SET', bucketKey, threshold)
        redis.call('SET', bucketRefillTimeKey, tonumber(nowMillis))
    end

    return threshold
end

-- 3. 트래픽 진입 가능 여부 판단
local function checkEntry(queueKey, bucketKey, entryCountKey, token, nowMillis, threshold)
    local rank = redis.call('ZRANK', queueKey, token)
    local bucketSize = tonumber(redis.call('GET', bucketKey)) or threshold
    local canEnter = false

    if (rank < threshold) and (bucketSize > 0) then
        canEnter = true
    else
        local waitingTime = tonumber(redis.call('ZSCORE', queueKey, token)) or 0
        local estimatedTime = math.ceil(rank + 1 / threshold) * 60000
        if tonumber(nowMillis) - waitingTime >= estimatedTime and bucketSize > 0 then
            canEnter = true
        end
    end

    if canEnter then
        redis.call('DECR', bucketKey)
        redis.call('INCR', entryCountKey)
        redis.call('ZREM', queueKey, token)
        return { 1, rank, 0, 0 }
    else
        local number = rank + 1
        local estimatedTime = math.ceil(number / threshold) * 60000
        local totalCount = redis.call('ZCARD', queueKey)
        return { 0, number, estimatedTime, totalCount }
    end
end

-- KEYS[1] = queueKey
-- KEYS[2] = thresholdKey
-- KEYS[3] = bucketKey
-- KEYS[4] = bucketRefillTimeKey
-- KEYS[5] = entryCountKey
local queueKey            = KEYS[1]
local thresholdKey        = KEYS[2]
local bucketKey           = KEYS[3]
local bucketRefillTimeKey = KEYS[4]
local entryCountKey       = KEYS[5]

-- ARGV[1] = token
-- ARGV[2] = score (timestamp)
-- ARGV[3] = nowMillis
-- ARGV[4] = defaultThreshold
local token               = ARGV[1]
local score               = ARGV[2]
local nowMillis           = ARGV[3]
local defaultThreshold    = ARGV[4]

-- 메인 실행 로직
addQueue(queueKey, token, score)
local threshold = checkBucket(thresholdKey, bucketKey, bucketRefillTimeKey, nowMillis, defaultThreshold)
return checkEntry(queueKey, bucketKey, entryCountKey, token, nowMillis, threshold)
