-- ARGV[1] = zoneId
-- ARGV[2] = token
-- ARGV[3] = score (timestamp or incremental ID)
-- ARGV[4] = now (current timestamp in seconds)

local zoneId = ARGV[1]
local zqueueKey = "ktc:zqueue:" .. zoneId
local tokenKey = "ktc:tokens:" .. zoneId
local lastRefillKey = "ktc:last_refill_ts:" .. zoneId
local thresholdKey = "ktc:threshold:" .. zoneId

local token = ARGV[2]
local score = tonumber(ARGV[3])
local now = tonumber(ARGV[4])
local defaultThreshold = 1000

-- 1. 사용자 대기열 등록 (중복 방지)
redis.call("ZADD", zqueueKey, "NX", score, token)

-- 2. 사용자 순번 조회
local waitingNumber = redis.call("ZRANK", zqueueKey, token)
if not waitingNumber then
  return {err = "User not found in zqueue after insert"}
end

-- 3. 현재 토큰 수, 마지막 리필 시각 조회
local availableTokens = tonumber(redis.call("GET", tokenKey)) or 0
local lastRefill = tonumber(redis.call("GET", lastRefillKey)) or 0

-- 4. 처리 속도 설정 조회
local threshold = tonumber(redis.call("GET", thresholdKey))
if not threshold or threshold <= 0 then
  --threshold = defaultThreshold
  threshold = 1
  redis.call("SET", thresholdKey, tostring(threshold))
end

-- 5. 리필 필요 여부 판단 (1분 단위)
if now - lastRefill >= 60 then
  availableTokens = threshold
  redis.call("SET", tokenKey, tostring(availableTokens))
  redis.call("SET", lastRefillKey, tostring(now))
end

-- 6. 진입 가능 여부 판단
local estimatedTime = 0

if waitingNumber < availableTokens then
  -- 진입 가능한 경우
  redis.call("ZREM", zqueueKey, token)
  redis.call("DECRBY", tokenKey, 1)
else
  -- 대기해야 하는 경우
  -- waitingNumber는 0부터 시작하므로, 실제 대기 순번은 waitingNumber + 1
  -- availableTokens는 현재 사용 가능한 토큰 수
  -- threshold는 분당 처리 가능한 토큰 수
  estimatedTime = math.floor((waitingNumber - availableTokens + 1) * 60 / threshold)
end

local totalCount = redis.call("ZCARD", zqueueKey)

return {
  waitingNumber + 1,
  estimatedTime,
  totalCount
} 