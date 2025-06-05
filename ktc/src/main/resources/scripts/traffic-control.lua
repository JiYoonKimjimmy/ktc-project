local queueKey = KEYS[1]
local queueStatusKey = KEYS[2]
local thresholdKey = KEYS[3]
local slotWindowKey = KEYS[4]
local entryCountKey = KEYS[5]
local tokenLastPollingTimeKey = KEYS[6]

local token = ARGV[1]
local nowMilli = tonumber(ARGV[2])
local defaultThreshold = tonumber(ARGV[3])

local queueStatus = redis.call('GET', queueStatusKey) or "ACTIVE"
if queueStatus == 'BLOCKED' then
    return { -1, 0, 0, 0 }
end

-- 1. threshold 조회
redis.call('SETNX', thresholdKey, defaultThreshold)
local threshold = tonumber(redis.call('GET', thresholdKey))

-- 2. 6초 slot 계산
local minute = math.floor(nowMilli / 60000)
local secondInMinute = math.floor((nowMilli % 60000) / 1000)
local slot = math.floor(secondInMinute / 6) -- 0~9
local allowedPer6Sec = math.floor(threshold / 10)
if allowedPer6Sec <= 1 then allowedPer6Sec = 1 end

-- 3. windowKey 생성
local windowKey = slotWindowKey .. ":" .. minute .. ":" .. slot

-- 4. 대기열 Queue 토큰 추가
if redis.call('ZSCORE', queueKey, token) == false then
    redis.call('ZADD', queueKey, nowMilli, token)
end

-- 5. 현재 slot 진입 Count 조회
local currentCount = tonumber(redis.call('GET', windowKey) or '0')

-- 6. 진입 허용 조건 확인: (slot 내 진입 Count) < allowedPer6Sec && (대기 순번) < allowedPer6Sec
local rank = tonumber(redis.call('ZRANK', queueKey, token))
if currentCount < allowedPer6Sec and rank < allowedPer6Sec then
    -- token 진입 허용
    redis.call('INCR', windowKey)
    redis.call('INCR', entryCountKey)
    redis.call('ZREM', queueKey, token)
    redis.call('ZREM', tokenLastPollingTimeKey, token)
    -- windowKey 1분 만료 설정
    redis.call('PEXPIRE', windowKey, 60000)
    local totalCount = redis.call('ZCARD', queueKey)
    return { 1, 0, 0, totalCount }
else
    -- token 진입 대기
    redis.call('ZADD', tokenLastPollingTimeKey, nowMilli, token)

    local waitSlot = math.floor(rank / allowedPer6Sec)
    local nextSlotStart = nowMilli - (nowMilli % 6000) + (waitSlot * 6000) + 6000
    local waitTime = nextSlotStart - nowMilli
    local totalCount = redis.call('ZCARD', queueKey)

    return { 0, rank + 1, waitTime, totalCount }
end