-- 1. 트래픽 요청 토큰 Queue 저장
local function addToQueue(queueKey, token, score)
    redis.call('ZADD', queueKey, 'NX', score, token)
end

-- 2. 토큰-버킷 리필 시간 확인 및 리필 처리
local function checkAndRefillBucket(queueCursorKey, thresholdKey, bucketKey, bucketRefillTimeKey, nowMillis, defaultThreshold)
    redis.call('SETNX', queueCursorKey, 0)
    redis.call('SETNX', thresholdKey, defaultThreshold)
    redis.call('SETNX', bucketKey, defaultThreshold)
    redis.call('SETNX', bucketRefillTimeKey, nowMillis)

    local bucketRefillTime = tonumber(redis.call('GET', bucketRefillTimeKey))
    local threshold = tonumber(redis.call('GET', thresholdKey)) or tonumber(defaultThreshold)

    if tonumber(nowMillis) - bucketRefillTime >= 60000 then
        redis.call('INCRBY', queueCursorKey, threshold)
        redis.call('SET', bucketKey, threshold)
        redis.call('SET', bucketRefillTimeKey, tonumber(nowMillis))
    end

    return threshold
end

-- 3. 트래픽 진입 가능 여부 판단
local function checkTrafficEntry(queueKey, queueCursorKey, bucketKey, token, threshold)
    local rank = redis.call('ZRANK', queueKey, token) or -1
    local queueCursor = tonumber(redis.call('GET', queueCursorKey)) or 0
    local bucketSize = tonumber(redis.call('GET', bucketKey)) or threshold
    
    local canEnter = (bucketSize > 0) and (rank >= queueCursor and rank < queueCursor + threshold)

    if canEnter then
        redis.call('DECR', bucketKey)
        return { 1, rank, 0, 0 } -- number, estimatedTime, totalCount
    else
        local queueSize = redis.call('ZCARD', queueKey)
        local number = rank - queueCursor - threshold - bucketSize + 1
        local estimatedTime = math.ceil(number / threshold) * 60000
        local totalCount = queueSize - queueCursor - threshold - bucketSize
        return { 0, number, estimatedTime, totalCount }
    end
end

-- KEYS[1] = queueKey (ZSet)
-- KEYS[2] = queueCursorKey
-- KEYS[3] = bucketKey
-- KEYS[4] = bucketRefillTimeKey
-- KEYS[5] = thresholdKey
local queueKey            = KEYS[1]
local queueCursorKey      = KEYS[2]
local bucketKey           = KEYS[3]
local bucketRefillTimeKey = KEYS[4]
local thresholdKey        = KEYS[5]

-- ARGV[1] = token
-- ARGV[2] = score (timestamp)
-- ARGV[3] = nowMillis
-- ARGV[4] = defaultThreshold
local token               = ARGV[1]
local score               = ARGV[2]
local nowMillis           = ARGV[3]
local defaultThreshold    = ARGV[4]

-- 메인 실행 로직
addToQueue(queueKey, token, score)
local threshold = checkAndRefillBucket(queueCursorKey, thresholdKey, bucketKey, bucketRefillTimeKey, nowMillis, defaultThreshold)
return checkTrafficEntry(queueKey, queueCursorKey, bucketKey, token, threshold)
