-- KEYS[1] = zqueueKey
-- KEYS[2] = tokenKey
-- KEYS[3] = lastRefillKey
-- KEYS[4] = thresholdKey
local zqueueKey = KEYS[1]
local tokenKey = KEYS[2]
local lastRefillKey = KEYS[3]
local thresholdKey = KEYS[4]

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
local lastRefill = tonumber(redis.call("GET", lastRefillKey)) or 0

local threshold = tonumber(redis.call("GET", thresholdKey))
if not threshold or threshold <= 0 then
  threshold = defaultThreshold
  redis.call("SET", thresholdKey, tostring(threshold))
end

if now - lastRefill >= 60 then
  availableTokens = threshold
  redis.call("SET", tokenKey, tostring(availableTokens))
  redis.call("SET", lastRefillKey, tostring(now))
end

local estimatedTime = 0

if waitingNumber < availableTokens then
  redis.call("ZREM", zqueueKey, token)
  redis.call("DECRBY", tokenKey, 1)
else
  estimatedTime = math.floor((waitingNumber - availableTokens + 1) * 60 / threshold)
end

local totalCount = redis.call("ZCARD", zqueueKey)

return {
  waitingNumber + 1,
  estimatedTime,
  totalCount
}
