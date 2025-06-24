local queue_key = KEYS[1]
local token_last_polling_time_key = KEYS[2]

local now = tonumber(ARGV[1])
local batch_size = 5000
local max_loops = 50

-- 만료 기준 시간 계산 = 현재 시간 - 1분
local expired_score = now - 60000

local total_deleted = 0

for i = 1, max_loops do
    -- 만료 대상 token을 batch_size만큼 조회
    local expired_tokens = redis.call('ZRANGEBYSCORE', token_last_polling_time_key, '-inf', expired_score, 'LIMIT', 0, batch_size)
    if #expired_tokens == 0 then
        break
    end
    redis.call('ZREM', queue_key, unpack(expired_tokens))
    redis.call('ZREM', token_last_polling_time_key, unpack(expired_tokens))
    total_deleted = total_deleted + #expired_tokens
    -- 1000개 미만이면 더 이상 남은 만료 토큰이 없음
    if #expired_tokens < batch_size then
        break
    end
end

return total_deleted