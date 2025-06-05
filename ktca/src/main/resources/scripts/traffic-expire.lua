local queue_key = KEYS[1]
local token_last_polling_time_key = KEYS[2]

local now = tonumber(ARGV[1])

-- 만료 기준 시간 계산 = 현재 시간 - 1분
local expired_score = now - 60000

-- 만료 대상 token 조회 (score <= expired_score)
local expired_tokens = redis.call('ZRANGEBYSCORE', token_last_polling_time_key, '-inf', expired_score)

if #expired_tokens > 0 then
    redis.call('ZREM', queue_key, unpack(expired_tokens))
    redis.call('ZREM', token_last_polling_time_key, unpack(expired_tokens))
end

return #expired_tokens