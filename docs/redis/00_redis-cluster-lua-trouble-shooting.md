# Redis Cluster + Lua Script 연동 관련 Trouble Shooting 🥵

## Redis Cluster + Lua Script 실행 시 "non-local key" 에러 해결하기

### 문제 상황

- Redis Cluster 환경 구축하여 Lua Script 실행 요청한 결과 에러 발생
- 기존 Lua Script 내부에는 스크립트 수행에 필요한 Cache Key 자체 정의
- Lua Script 실행한 Redis 노드에서 Key 를 조회(참조/접근)할 수 없기에 에러 발생 

```redis
local zqueueKey = "ktc:zqueue:" .. zoneId
local tokenKey = "ktc:tokens:" .. zoneId
local lastRefillKey = "ktc:last_refill_time:" .. zoneId
local thresholdKey = "ktc:threshold:" .. zoneId

-- 이후 redis.call()로 여러 키 접근
```

```log
# Lua Script 내부에서 Key 선언하는 경우
... ERR Script attempted to access a non local key in a cluster node script ...
# Key 해시 태그 없이 Key 선언하는 경우
... CROSSSLOT Keys in request don't hash to the same slot. ...
```

### 원인 분석

#### Redis Cluster 는 Key 를 Hash Slot 다르게 분산 저장하기 때문!!

- Redis 는 전체 Key 공간을 **16384개의 해시 슬롯**으로 나눠서, 각 key 를 `CRC16(key) % 16384` 방식으로 슬롯에 배치
- 하지만, **Lua Script 는 단일 노드에서 실행**하기 때문에 **동일한 해시 슬롯에 있는 Key 만 접근 가능**

### 해결 방법

#### 1. `Hash Tag` 활용하여 해시 슬롯 통일

- Redis 는 **`{}`(해시 태그)** 안 문자열을 **해시 슬롯 계산에 사용**
- 같은 해시 태그를 가진 Key 는 Redis 는 같은 해시 슬롯에 저장

##### Before

```redis
"ktc:zqueue:zone1"
"ktc:tokens:zone1"
```

##### After

```redis
"ktc:{zone1}:zqueue"
"ktc:{zone1}:tokens"
```

#### 2. Lua Script 호출 시 `KEYS[]` 를 명확하게 전달

- Lua Script 안에서 Key 를 자체 정의하지 않고, 외부에서 명확하게 전달
- Lua Script 안에서는 전달된 Key 를 `KEYS[]` 를 통해서 활용

##### Before

```kotlin
val args = listOf(token, score.toString(), now.toString())
// key 전달 부분 `emptyList()` 로 전달
stringRedisTemplate.execute(script, emptyList(), *args.toTypedArray())
```

```redis
-- 스크립트 안 자체 정의한 Key
local zqueueKey = "ktc:zqueue:" .. zoneId
local tokenKey = "ktc:tokens:" .. zoneId
local lastRefillKey = "ktc:last_refill_time:" .. zoneId
local thresholdKey = "ktc:threshold:" .. zoneId
```

##### After

```kotlin
val keys = listOf(
    "ktc:{zone1}:zqueue",
    "ktc:{zone1}:tokens"
)
val args = listOf(
    token,
    score.toString(),
    now.toString()
)
// key 목록을 명확하게 전달
stringRedisTemplate.execute(script, keys, *args.toTypedArray())
```

```redis
-- 외부에서 전달된 Key 활용
-- KEYS[1] = zqueueKey
-- KEYS[2] = tokenKey

local zqueueKey = KEYS[1]
local tokenKey = KEYS[2]
```

---

### 결론

#### Redis Cluster 환경에서 Lua 스크립트 사용할 때는 Key 슬롯 확인!!

- Key 를 해시 태그 `{}` 묶어서 같은 슬롯에 배치
- Lua 스크립트 내부 `KEYS`, `ARGV` 구분하여 사용
- Redis Client 호출 시 `keys` 명시적으로 전달

---
