-- 트래픽 제어 Lua Script (밀리초 기준 대기열 + 윈도우별 카운터)
-- KEYS: [queueKey, queueStatusKey, thresholdKey, entryCountKey]
-- ARGV: [token, nowMilli, defaultThreshold]

local queueKey = KEYS[1]
local queueStatusKey = KEYS[2]
local thresholdKey = KEYS[3]
local entryCountKey = KEYS[4]

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

local windowKey = entryCountKey .. ":" .. minute .. ":" .. slot

-- 3. 대기열에 토큰 추가 (score=nowMs, 이미 있으면 갱신X)
if redis.call('ZSCORE', queueKey, token) == false then
    redis.call('ZADD', queueKey, nowMilli, token)
end

-- 4. 내 순번 확인 (ZRANK 0부터 시작)
local queuePos = redis.call('ZRANK', queueKey, token)
if not queuePos then
    return { -1, 0, 0, 0 }
end
queuePos = tonumber(queuePos)

-- 5. 현재 slot의 진입 카운터 조회
local currentCount = tonumber(redis.call('GET', windowKey) or '0')
local queueSize = redis.call('ZCARD', queueKey)

-- 6. 진입 허용 조건: slot 내 진입 카운터 < allowedPer6Sec && 내 순번 < allowedPer6Sec
if currentCount < allowedPer6Sec and queuePos < allowedPer6Sec then
    -- 진입 허용
    redis.call('INCR', windowKey)
    redis.call('INCR', entryCountKey)
    redis.call('ZREM', queueKey, token)
    redis.call('PEXPIRE', windowKey, 60000)
    return { 1, 0, 0, queueSize }
else
    -- 대기: 내 순번/allowedPer6Sec의 올림 * 6초
    local waitSlot = math.floor(queuePos / allowedPer6Sec)
    local nextSlotStart = nowMilli - (nowMilli % 6000) + (waitSlot * 6000) + 6000
    local waitTime = nextSlotStart - nowMilli
    return { 0, queuePos + 1, waitTime, queueSize }
end