local queueKey = KEYS[1]
local queueStatusKey = KEYS[2]
local thresholdKey = KEYS[3]
local entryWindowKey = KEYS[4]
local entrySlotKey = KEYS[5]
local entryCountKey = KEYS[6]
local tokenLastPollingTimeKey = KEYS[7]

local token = ARGV[1]
local nowMilli = tonumber(ARGV[2])
local defaultThreshold = tonumber(ARGV[3])

local ONE_MINUTE_MILLIS = 60000
local ONE_SECONDS_MILLIS = 1000
local SIX_SECONDS_MILLIS = 6 * ONE_SECONDS_MILLIS

local queueStatus = redis.call('HMGET', queueStatusKey, 'status', 'activationTime')
local status = queueStatus[1]
local activationTime = tonumber(queueStatus[2] or nowMilli)

-- 0. Queue status & activationTime 확인 후 진입 or 차단 처리
if not status or status == '' then
    -- Queue status 정보 없는 경우, 차단('TRAFFIC_ZONE_NOT_FOUND') 처리
    return { -100, 0, 0, 0 }
elseif status == 'BLOCKED' then
    -- Queue status 'BLOCKED' 인 경우, 차단('TRAFFIC_ZONE_BLOCKED') 처리
    return { -102, 0, 0, 0 }
elseif status == 'FAULTY_503' then
    -- Queue status 'FAULTY_503' 인 경우, 진입 장애 차단('FAULTY_503_ERROR') 처리
    return { -503, 0, 0, 0 }
elseif activationTime ~= nil and nowMilli < activationTime then
    -- Queue activation 시간이 현재보다 이전인 경우, 진입 처리
    return { 1, 0, 0, 0 }
end

-- 1. threshold 조회
redis.call('SETNX', thresholdKey, defaultThreshold)
local threshold = tonumber(redis.call('GET', thresholdKey))

-- 2. 6초 slot 계산
local minute = math.floor(nowMilli / ONE_MINUTE_MILLIS)
local secondInMinute = math.floor((nowMilli % ONE_MINUTE_MILLIS) / ONE_SECONDS_MILLIS)
local slot = math.floor(secondInMinute / 6)
local allowedPer6Sec = math.floor(threshold / 10)
if allowedPer6Sec < 1 then allowedPer6Sec = 1 end

-- 3. window & slotCount count 조회
local windowCountKey = entryWindowKey .. ":" .. minute
local slotCountKey = entrySlotKey .. ":" .. minute .. ":" .. slot
local windowEntryCount = tonumber(redis.call('GET', windowCountKey) or '0')
local slotEntryCount = tonumber(redis.call('GET', slotCountKey) or '0')
local queueSize = tonumber(redis.call('ZCARD', queueKey))

-- 4. 대기열 Queue 토큰 추가
local score = redis.call('ZSCORE', queueKey, token)
if not score then
    redis.call('ZADD', queueKey, nowMilli, token)
    score = nowMilli
else
    score = tonumber(score)
end

-- 5. 진입 허용 조건 확인: readyTime = token 진입 시점 + (slot * 6초) + 6초
local rank = tonumber(redis.call('ZRANK', queueKey, token))

local canEnter = false
if windowEntryCount < threshold and queueSize == 0 then
    canEnter = true
elseif slotEntryCount < allowedPer6Sec and rank < allowedPer6Sec then
    canEnter = true
end

if canEnter then
    -- token 진입 허용
    redis.call('INCR', windowCountKey)
    redis.call('INCR', slotCountKey)
    redis.call('INCR', entryCountKey)
    redis.call('ZREM', queueKey, token)
    redis.call('ZREM', tokenLastPollingTimeKey, token)
    redis.call('PEXPIRE', windowCountKey, ONE_MINUTE_MILLIS)
    redis.call('PEXPIRE', slotCountKey, ONE_MINUTE_MILLIS)
    local totalCount = tonumber(redis.call('ZCARD', queueKey))
    return { 1, 0, 0, totalCount }
else
    -- token 진입 대기
    local estimatedTime = math.max((rank + 1) / (threshold / ONE_MINUTE_MILLIS) , 3 * ONE_SECONDS_MILLIS)
    local totalCount = tonumber(redis.call('ZCARD', queueKey))
    redis.call('ZADD', tokenLastPollingTimeKey, nowMilli, token)
    return { 0, rank + 1, estimatedTime, totalCount }
end