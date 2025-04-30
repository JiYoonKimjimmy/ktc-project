-- KEYS[1] = zqueueKey
-- KEYS[2] = lastEntryKey
local zqueueKey = KEYS[1]
local lastEntryTimeKey = KEYS[2]

-- 마지막 트래픽 진입 시점 조회
local lastEntryTime = tonumber(redis.call("GET", lastEntryTimeKey)) or 0

-- 만료 시간 계산 (마지막 트래픽 진입 시점 - 1분)
local expirationTime = lastEntryTime - 60

-- 만료된 토큰 조회 및 삭제 (만료 시간 이전의 모든 토큰)
local expiredTokens = redis.call("ZRANGEBYSCORE", zqueueKey, "-inf", expirationTime)
local count = 0

if #expiredTokens > 0 then
    -- 만료된 토큰 삭제
    redis.call("ZREMRANGEBYSCORE", zqueueKey, "-inf", expirationTime)
    count = #expiredTokens
end

return count