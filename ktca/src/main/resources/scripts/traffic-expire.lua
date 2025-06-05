--[[
트래픽 만료 프로세스 Lua 스크립트 (Redis Cluster 대응)
- KEYS: [queueKey1, thresholdKey1, tokenLastPollingTimeKey1, queueKey2, thresholdKey2, tokenLastPollingTimeKey2, ...]
- ARGV[1]: now (current epoch millis)
- ARGV[2]: six_seconds_millis (6초 ms)
]]
local keys = #KEYS / 2
local now = tonumber(ARGV[1])

for i = 0, keys - 1 do
    local queue_key = KEYS[i * 2 + 1]
    local token_last_polling_time_key = KEYS[i * 2 + 2]

    -- 만료 기준 시간 계산 = 현재 시간 - 1분
    local expired_score = now - 60000

    -- 만료 대상 token 조회 (score <= expired_score)
    local expired_tokens = redis.call('ZRANGEBYSCORE', token_last_polling_time_key, '-inf', expired_score)

    if #expired_tokens > 0 then
        redis.call('ZREM', queue_key, unpack(expired_tokens))
        redis.call('ZREM', token_last_polling_time_key, unpack(expired_tokens))
    end
end

return 1