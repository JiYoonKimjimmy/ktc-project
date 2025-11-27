# KTC 트래픽 제어에서 Sorted Set을 사용하는 이유

## 개요

KTC 프로젝트는 Redis Lua Script를 통해 트래픽 제어를 구현하고 있으며, 대기열 관리를 위해 **Sorted Set** 자료구조를 사용합니다.
이 문서는 단순한 Set이나 Queue(List) 대신 Sorted Set을 선택한 명확한 이유를 분석합니다.

## Sorted Set이 사용되는 위치

### traffic-control.lua

```lua
-- Line 52: 대기열 크기 조회
local queueSize = tonumber(redis.call('ZCARD', queueKey))

-- Line 55-58: Token의 score 조회 및 추가
local score = redis.call('ZSCORE', queueKey, token)
if not score then
    redis.call('ZADD', queueKey, nowMilli, token)
    score = nowMilli
end

-- Line 64: Token의 순위(rank) 조회
local rank = tonumber(redis.call('ZRANK', queueKey, token))

-- Line 78: 진입 허용 시 Token 제거
redis.call('ZREM', queueKey, token)

-- Line 86: rank 기반 예상 대기 시간 계산
local estimatedTime = math.max((rank + 1) / (threshold / ONE_MINUTE_MILLIS), 3 * ONE_SECONDS_MILLIS)

-- Line 88: 마지막 polling 시간 기록
redis.call('ZADD', tokenLastPollingTimeKey, nowMilli, token)
```

### traffic-expire.lua

```lua
-- Line 15: 만료된 token 조회 (1분 이상 polling 없는 경우)
local expired_tokens = redis.call('ZRANGEBYSCORE', token_last_polling_time_key, '-inf', expired_score, 'LIMIT', 0, batch_size)

-- Line 19-20: 만료된 token 일괄 삭제
redis.call('ZREM', queue_key, unpack(expired_tokens))
redis.call('ZREM', token_last_polling_time_key, unpack(expired_tokens))
```

## Sorted Set을 선택한 핵심 이유

### 1. 시간 기반 정렬 + 순서 보장 (FIFO)

#### 구현 방식
```lua
-- score로 현재 시간(밀리초)을 사용
redis.call('ZADD', queueKey, nowMilli, token)
```

#### 효과
- **자동 정렬**: score(현재 시간)를 기준으로 자동 정렬
- **FIFO 보장**: 먼저 요청한 token이 자동으로 앞 순서에 위치
- **공정성**: 시간 순으로 진입 기회 부여

#### 다른 자료구조와 비교

| 자료구조 | 시간 순 정렬 | 문제점 |
|---------|------------|--------|
| **Sorted Set** | ✅ score로 자동 정렬 | - |
| **Set** | ❌ 순서 없음 | 누가 먼저 왔는지 알 수 없음 |
| **Queue(List)** | ✅ FIFO 가능 | 순서는 보장되나 순위 조회가 느림 |

### 2. 대기열 순위(rank) 계산 O(log N)

#### 구현 방식
```lua
-- ZRANK: Sorted Set에서 member의 순위를 O(log N)에 조회
local rank = tonumber(redis.call('ZRANK', queueKey, token))

-- rank를 활용한 예상 대기 시간 계산
local estimatedTime = math.max((rank + 1) / (threshold / ONE_MINUTE_MILLIS), 3 * ONE_SECONDS_MILLIS)
```

#### 효과
- **빠른 순위 조회**: O(log N) 시간 복잡도
- **사용자 경험 향상**: "현재 N번째 대기 중입니다" 정보 제공 가능
- **예상 대기 시간 계산**: rank 기반으로 대기 시간 추정

#### 성능 비교 (대기열에 1000명이 있는 경우)

| 자료구조 | 순위 조회 시간 복잡도 | 실제 비교 횟수 |
|---------|-------------------|--------------|
| **Sorted Set** | O(log N) | ~10회 |
| **Set** | ❌ 불가능 | - |
| **Queue(List)** | O(N) | ~1000회 |

```lua
-- Sorted Set 사용 시
local rank = redis.call('ZRANK', queueKey, token)  -- O(log 1000) ≈ 10번 비교

-- Queue(List) 사용 시
local pos = redis.call('LPOS', queueKey, token)    -- O(1000) = 1000번 탐색
```

### 3. 특정 Token 존재 여부 확인 O(1)

#### 구현 방식
```lua
-- ZSCORE: 특정 token이 대기열에 있는지 O(1)에 확인
local score = redis.call('ZSCORE', queueKey, token)
if not score then
    -- 대기열에 없으면 추가
    redis.call('ZADD', queueKey, nowMilli, token)
end
```

#### 효과
- **중복 방지**: 같은 token이 여러 번 추가되지 않도록 방지
- **빠른 확인**: O(1) 시간에 존재 여부 확인
- **원자적 처리**: Lua Script 내에서 확인 후 추가를 원자적으로 수행

#### 성능 비교

| 자료구조 | 존재 확인 | member 삭제 |
|---------|----------|-----------|
| **Sorted Set** | O(1) - ZSCORE | O(log N) - ZREM |
| **Set** | O(1) - SISMEMBER | O(1) - SREM |
| **Queue(List)** | O(N) - LPOS | O(N) - LREM |

### 4. 범위 기반 조회 및 만료 처리

#### 구현 방식 (traffic-expire.lua)
```lua
-- 만료 기준 시간 = 현재 시간 - 1분
local expired_score = now - 60000

-- ZRANGEBYSCORE: score가 특정 범위인 member들을 효율적으로 조회
local expired_tokens = redis.call(
    'ZRANGEBYSCORE',
    token_last_polling_time_key,
    '-inf',
    expired_score,
    'LIMIT', 0, batch_size
)

-- 만료된 token들을 일괄 삭제
redis.call('ZREM', queue_key, unpack(expired_tokens))
redis.call('ZREM', token_last_polling_time_key, unpack(expired_tokens))
```

#### 효과
- **효율적인 만료 처리**: 시간 기반 범위 조회로 만료된 token만 선택적으로 조회
- **배치 처리**: 만료된 token들을 한 번에 찾아서 일괄 삭제
- **메모리 관리**: 1분 이상 polling 없는 token을 자동 정리

#### 다른 자료구조의 문제점

| 자료구조 | 만료 처리 방식 | 문제점 |
|---------|--------------|--------|
| **Sorted Set** | ZRANGEBYSCORE로 범위 조회 | ✅ 효율적 |
| **Set** | 전체 스캔 필요 | ❌ O(N), 각 member의 시간 정보를 별도 저장 필요 |
| **Queue(List)** | 앞에서부터 확인 | ⚠️ 중간에 만료된 token 처리 어려움 |

### 5. 조건부 진입 제어

#### 구현 방식
```lua
-- Line 69-70: 현재 slot 허용량과 rank를 비교하여 진입 제어
local canEnter = false
if windowEntryCount < threshold and queueSize == 0 then
    canEnter = true
elseif slotEntryCount < allowedPer6Sec and rank < allowedPer6Sec then
    canEnter = true
end
```

#### 효과
- **공정한 진입 제어**: 대기열 앞쪽 N명만 진입 허용
- **순서 보장**: 먼저 온 사용자(낮은 score)가 먼저 진입
- **트래픽 분산**: 6초 slot당 허용 인원 제어

#### 예시 (threshold=100, allowedPer6Sec=10)

```lua
-- 대기열에 50명이 있는 상황
-- rank 0~9 (앞 10명): canEnter = true
-- rank 10~49: canEnter = false

if rank < allowedPer6Sec then  -- rank < 10
    canEnter = true
end
```

**Set을 사용했다면**: 순서 개념이 없어서 누구를 먼저 진입시킬지 결정 불가 ❌

## 각 자료구조 종합 비교

### 요구사항별 비교표

| 요구사항 | Sorted Set | Set | Queue(List) |
|---------|-----------|-----|------------|
| **시간 순 정렬** | ✅ score로 자동 정렬 | ❌ 순서 없음 | ✅ FIFO 가능 |
| **순위(rank) 조회** | ✅ O(log N) | ❌ 불가능 | ⚠️ O(N) |
| **중복 방지** | ✅ Set 특성 | ✅ Set 특성 | ❌ 중복 가능 |
| **특정 member 존재 확인** | ✅ O(1) - ZSCORE | ✅ O(1) - SISMEMBER | ⚠️ O(N) - LPOS |
| **특정 member 삭제** | ✅ O(log N) - ZREM | ✅ O(1) - SREM | ⚠️ O(N) - LREM |
| **범위 기반 조회** | ✅ ZRANGEBYSCORE | ❌ 불가능 | ❌ 불가능 |
| **만료 처리** | ✅ 시간 기반 효율적 | ❌ 전체 스캔 필요 | ⚠️ 비효율적 |
| **공정성 보장** | ✅ 시간 순 보장 | ❌ 순서 없음 | ✅ FIFO |

### 성능 비교 (대기열에 1000명)

| 작업 | Sorted Set | Set | Queue(List) |
|-----|-----------|-----|------------|
| **Token 추가** | O(log N) ≈ 10 | O(1) | O(1) |
| **순위 조회** | O(log N) ≈ 10 | ❌ 불가능 | O(N) = 1000 |
| **중복 확인** | O(1) | O(1) | O(N) = 1000 |
| **만료 처리** | O(log N + M) | O(N) = 1000 | O(N) = 1000 |

> M = 만료된 token 수, N = 전체 token 수

## 실제 사용 시나리오 예시

### 시나리오: 1000명이 동시에 대기열 진입

#### Sorted Set 사용 시 ✅

```lua
-- 1. Token 추가: O(log N)
redis.call('ZADD', queueKey, nowMilli, token)  -- O(log 1000) ≈ 10번 비교

-- 2. 순위 확인: O(log N)
local rank = redis.call('ZRANK', queueKey, token)  -- O(log 1000) ≈ 10번 비교

-- 3. 예상 대기 시간 계산: O(1)
local estimatedTime = (rank + 1) / (threshold / 60000)

-- 총 시간 복잡도: O(log N) - 매우 효율적
```

#### Queue(List) 사용 시 ⚠️

```lua
-- 1. Token 추가: O(1)
redis.call('RPUSH', queueKey, token)  -- O(1)

-- 2. 중복 확인: O(N)
local pos = redis.call('LPOS', queueKey, token)  -- O(1000) = 1000번 탐색

-- 3. 순위 확인: O(N)
-- LPOS 결과를 사용해야 함

-- 총 시간 복잡도: O(N) - 대기열이 길어질수록 느려짐
```

#### Set 사용 시 ❌

```lua
-- 1. Token 추가: O(1)
redis.call('SADD', queueKey, token)  -- O(1)

-- 2. 순위 확인: 불가능!
-- Set은 순서가 없어서 "몇 번째"를 알 수 없음
-- 대기 순서 정보를 제공할 수 없음

-- 순위 계산 불가능 - 대기열 기능 구현 불가
```

## Sorted Set의 핵심 장점 정리

### 1. 트래픽 제어 시스템의 핵심 요구사항 충족

```
✅ 시간 순 정렬 (먼저 온 사용자 우선)
✅ 빠른 순위 조회 (사용자 경험)
✅ 중복 방지 (같은 token 중복 진입 방지)
✅ 효율적 만료 처리 (메모리 관리)
✅ 공정한 진입 제어 (순서 기반)
```

### 2. 시간 복잡도 이점

| 작업 | 시간 복잡도 | 대기열 1000명 기준 |
|-----|-----------|-----------------|
| Token 추가 | O(log N) | ~10번 비교 |
| 순위 조회 | O(log N) | ~10번 비교 |
| 중복 확인 | O(1) | 1번 조회 |
| 만료 처리 | O(log N + M) | ~10 + M |

### 3. 코드 가독성 및 유지보수성

```lua
-- Sorted Set: 직관적이고 명확한 코드
local rank = redis.call('ZRANK', queueKey, token)
if rank < allowedPer6Sec then
    canEnter = true
end

-- Queue(List)를 사용했다면: 복잡하고 비효율적
local all_tokens = redis.call('LRANGE', queueKey, 0, -1)
local rank = 0
for i, t in ipairs(all_tokens) do
    if t == token then
        rank = i - 1
        break
    end
end
-- O(N) 시간 소요, 비효율적
```

## 결론

**Sorted Set은 트래픽 제어 시스템에서 필수적인 자료구조입니다.**

### 만약 다른 자료구조를 사용했다면

#### Set을 사용했다면 ❌
- **치명적 문제**: 순서 개념이 없음
- **불가능한 기능**:
  - 순위 조회 불가
  - 공정한 진입 제어 불가
  - 예상 대기 시간 계산 불가
- **결론**: 대기열 기능 구현 자체가 불가능

#### Queue(List)를 사용했다면 ⚠️
- **성능 문제**: 순위 조회 O(N)
- **확장성 문제**: 대기열이 길어질수록 급격한 성능 저하
- **예시**:
  - 대기열 100명: 100번 탐색
  - 대기열 1000명: 1000번 탐색
  - 대기열 10000명: 10000번 탐색
- **결론**: 트래픽이 많을수록 시스템 부하 증가

#### Sorted Set을 사용하면 ✅
- **성능**: O(log N) - 대기열이 길어져도 성능 안정적
- **기능**: 모든 요구사항 충족
- **확장성**: 대기열 크기와 무관하게 일정한 성능
- **예시**:
  - 대기열 100명: ~7번 비교
  - 대기열 1000명: ~10번 비교
  - 대기열 10000명: ~14번 비교
- **결론**: 대규모 트래픽 처리에 최적

### 핵심 요약

**Sorted Set은 "시간 순 정렬 + 빠른 순위 조회 + 중복 방지 + 효율적 만료 처리"를 모두 만족하는 유일한 자료구조이며, KTC 트래픽 제어 시스템의 성능과 확장성을 보장합니다.**

## 참고 코드 위치

- **traffic-control.lua**: `ktc/src/main/resources/scripts/traffic-control.lua`
  - Line 52: ZCARD (대기열 크기)
  - Line 55: ZSCORE (token 존재 확인)
  - Line 57: ZADD (token 추가)
  - Line 64: ZRANK (순위 조회)
  - Line 78: ZREM (token 삭제)
  - Line 86: rank 기반 예상 시간 계산

- **traffic-expire.lua**: `ktca/src/main/resources/scripts/traffic-expire.lua`
  - Line 15: ZRANGEBYSCORE (만료 token 범위 조회)
  - Line 19-20: ZREM (만료 token 일괄 삭제)

## 참고 자료

### Redis Sorted Set 공식 문서
- [Redis ZADD](https://redis.io/commands/zadd/)
- [Redis ZRANK](https://redis.io/commands/zrank/)
- [Redis ZSCORE](https://redis.io/commands/zscore/)
- [Redis ZRANGEBYSCORE](https://redis.io/commands/zrangebyscore/)
- [Redis ZREM](https://redis.io/commands/zrem/)
- [Redis Sorted Sets](https://redis.io/docs/data-types/sorted-sets/)

### 관련 문서
- [00_redis-cluster-lua-trouble-shooting.md](./00_redis-cluster-lua-trouble-shooting.md)
- [01_redis-lua-script-optimization.md](./01_redis-lua-script-optimization.md)
