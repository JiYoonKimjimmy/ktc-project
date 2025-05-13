-- KEYS[1] = zqueueKey
local zqueueKey = KEYS[1]

-- ARGV[1] = expirationTime
local expirationTime = tonumber(ARGV[1])

-- 만료 시간 이전 score 토큰 조회
local expiredTokens = redis.call("ZRANGEBYSCORE", zqueueKey, "-inf", expirationTime)
local count = 0

if #expiredTokens > 0 then
    -- 만료된 토큰 삭제
    redis.call("ZREMRANGEBYSCORE", zqueueKey, "-inf", expirationTime)
    count = #expiredTokens
end

return { count }