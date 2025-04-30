-- KEYS[1] = zqueueKey
-- KEYS[2] = tokenKey
-- KEYS[3] = lastRefillTimeKey
-- KEYS[4] = lastEntryKey
-- KEYS[5] = thresholdKey
local zqueueKey = KEYS[1]
local tokenKey = KEYS[2]
local lastRefillTimeKey = KEYS[3]
local lastEntryTimeKey = KEYS[4]
local thresholdKey = KEYS[5]

-- ARGV[1] = token
-- ARGV[2] = score
-- ARGV[3] = now
-- ARGV[4] = defaultThreshold
local token = ARGV[1]
local score = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local defaultThreshold = tonumber(ARGV[4]) or 1000

redis.call("ZADD", zqueueKey, "NX", score, token)

local waitingNumber = redis.call("ZRANK", zqueueKey, token)
if not waitingNumber then
  return {err = "User not found in zqueue after insert"}
end

local availableTokens = tonumber(redis.call("GET", tokenKey)) or 0
local lastRefillTime = tonumber(redis.call("GET", lastRefillTimeKey)) or 0

local threshold = tonumber(redis.call("GET", thresholdKey))
if not threshold or threshold <= 0 then
  threshold = defaultThreshold
  redis.call("SET", thresholdKey, tostring(threshold))
end

if now - lastRefillTime >= 60 then
  availableTokens = threshold
  redis.call("SET", tokenKey, tostring(availableTokens))
  redis.call("SET", lastRefillTimeKey, tostring(now))
end

local totalCount = redis.call("ZCARD", zqueueKey)

if waitingNumber < availableTokens then
  redis.call("ZREM", zqueueKey, token)
  redis.call("DECRBY", tokenKey, 1)

  local lastEntryTime = tonumber(redis.call("GET", lastEntryTimeKey)) or 0
  if score > lastEntryTime then
    redis.call("SET", lastEntryTimeKey, tostring(score))
  end

  return {
    waitingNumber + 1,  -- waiting.number
    0,                  -- waiting.estimatedTime
    totalCount          -- waiting.totalCount
  }
end

-- 1분 단위로 대기 시간 계산
local minutesToWait = math.ceil((waitingNumber - availableTokens + 1) / threshold)
local estimatedTime = minutesToWait * 60  -- 1분 = 60초

return {
  waitingNumber + 1,  -- waiting.number
  estimatedTime,      -- waiting.estimatedTime
  totalCount          -- waiting.totalCount
}
