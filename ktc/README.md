# KTC (Kona Traffic Controller) 😏

## Requirement

- `1분` 단위 최대 허용치`(Threshold)`만큼 트래픽 제한하여 대용량 트래픽 제어
    - Threshold 예상 범위 : `70K` ~ `100K`
- 실시간 or 실시간에 준하는 트래픽 진입 현황 정보 제공

---

## Architecture

### 트래픽 대기/진입 프로세스

- 임계치 설정 기반 트래픽 대기 처리
- 트래픽 대기 요청 시, 대기 순번 부여
- 현재 임계치 설정 값 기준, 해당 순번 대기 예상 시간 계산
- 트래픽 진입 요청 시, 진입 가능 여부 판단

> #### 예상 시나리오
> 
> 1. 초기 임계치 설정 : 2000
>     - 1 ~ 2000번: 즉시 입장 가능 (0분 대기)
>     - 2001 ~ 4000번: 1분 대기 후 입장
>     - 4001 ~ 6000번: 2분 대기 후 입장
> 2. 임계치 변경 : 2000 > 1000 감소
>     - 6001 ~ 7000번: 3분 대기 후 입장
>     - 7001 ~ 8000번: 4분 대기 후 입장
>     - 8001 ~ 9000번: 5분 대기 후 입장
>     - 9001 ~ 10000번: 6분 대기 후 입장
> 3. 임계치 변경 : 1000 > 500 감소
>     - 10001 ~ 10500번: 7분 대기 후 입장
>     - 10501 ~ 11000번: 8분 대기 후 입장
> 4. 임계치 변경 : 500 > 1000 증가
>     - 11001 ~ 12000번: 9분 대기 후 입장

---

### 트래픽 제어 프로세스

1. **트래픽 요청 토큰 Queue 저장**
   - 트래픽 대기 요청 토큰 `score`(현재 시간 밀리초) 기준 Queue(`ZSet`) 추가
   - 이미 동일 토큰 Queue 있는 경우, 추가하지 않음
2. **토큰-버킷 리필 시간 확인 및 리필 처리**
   - `현재 시간 - bucketRefillTime > 60000ms(1분)` 인 경우, `queueCursor` & `bucket` & `bucketRefillTime` 업데이트
     - `queueCursor`: `threshold` 만큼 증가시켜 Cursor 이동
     - `bucket`: `threshold` 값으로 토큰-버킷 리필
     - `bucketRefillTime`: 현재 시간으로 업데이트

3. **트래픽 진입 가능 여부 판단**
   - 현재 토큰의 rank(Queue 내 순번), queueCursor, bucketSize를 조회
   - 진입 가능 조건:
     - bucketSize > 0
     - queueCursor <= rank < queueCursor + threshold

   - **진입 가능한 경우:**
     - bucketSize를 1 감소(decrement)
     - 즉시 진입(TrafficWaiting.entry()) 반환

   - **진입 불가(대기)한 경우:**
     - 전체 Queue 크기(queueSize) 조회
     - 대기 순번(number), 예상 대기 시간(estimatedTime), 전체 대기 인원(totalCount) 계산
       - number = rank - queueCursor - threshold - bucketSize + 1
       - estimatedTime = ceil(number / threshold) * 1분
       - totalCount = queueSize - queueCursor - threshold - bucketSize
     - 대기 정보(TrafficWaiting.waiting) 반환

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant Redis

    Client->>Server: 트래픽 대기/진입 요청 (token, now)
    Server->>Redis: ZSet에 token 존재 여부 확인
    alt 토큰 없음
        Server->>Redis: ZSet에 token 추가 (score)
    end
    Server->>Redis: bucketRefillTime, threshold, bucket, queueCursor 조회
    alt now - bucketRefillTime > 1분 경과
        Server->>Redis: queueCursor(+=threshold), bucket(=threshold), bucketRefillTime(=now) 갱신
    end
    Server->>Redis: token's rank, bucketSize 조회
    canEnter = (bucketSize > 0) && (rank in queueCursor until (queueCursor + threshold))
    alt canEnter: true(진입 가능)
        Server->>Redis: bucket 차감
        Server-->>Client: 진입 성공 응답 (canEnter=true)
    else canEnter: false(진입 불가)
        Server->>Redis: queueSize 조회
        Server-->>Client: 대기 정보 응답 (canEnter=false, number, estimatedTime, totalCount)
    end
```

---

## Implementation

### 네트워크 처리 방식

- **HTTP Long-Term Polling (선정)** : 일정 주기 `Client > Server` HTTP 요청하여 반복 메시지 전송 방식
- HTTP SSEs : `Client < Server` 단방향 메시지 전송 가능한 HTTP Streaming 방식 
- WebSocket : `Client <> Server` 양방향 메시지 전송 가능한 TCP Socket 방식

> **HTTP Long-Term Polling 선정 이유** : 요구 사항을 충족하며, 긴 주기 Polling 방식은 서버 부하를 방지할 수 있는 방법 중 하나로 판단하여 선정

### 트래픽 제어 처리 방식

- `Token-Bucket` 알고리즘 적용한 트래픽 제어 처리

#### `Token-Bucket` 알고리즘 적용한 트래픽 제어 처리

- `Redis + Lua Script` 활용하여 **Caching Atomic** 원자성 보장하는 `Token-Bucket` 알고리즘 구현
- 트래픽 진입 허용 임계치만큼 보유한 `Token` 모두 소진하는 경우 트래픽 대기 처리

```redis
-- ARGV[1] = userToken
-- ARGV[2] = score (timestamp or incremental ID)
-- ARGV[3] = now (current timestamp in seconds)
 
local zqueueKey = "ktc:zqueue"
local tokenKey = "ktc:tokens"
local lastRefillKey = "ktc:last_refill_time"
local thresholdKey = "ktc:threshold"
local defaultRate = 1000
 
local userToken = ARGV[1]
local score = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
 
-- 1. 사용자 대기열 등록 (중복 방지)
redis.call("ZADD", zqueueKey, "NX", score, userToken)
 
-- 2. 사용자 순번 조회
local waitingNumber = redis.call("ZRANK", zqueueKey, userToken)
if not waitingNumber then
  return {err = "User not found in zqueue after insert"}
end
 
-- 3. 현재 토큰 수, 마지막 리필 시각 조회
local availableTokens = tonumber(redis.call("GET", tokenKey)) or 0
local lastRefill = tonumber(redis.call("GET", lastRefillKey)) or 0
 
-- 4. 처리 속도 설정 조회
local threshold = tonumber(redis.call("GET", thresholdKey))
if not threshold or threshold <= 0 then
  threshold = defaultRate
  redis.call("SET", thresholdKey, tostring(threshold))
end
 
-- 5. 리필 필요 여부 판단 (1분 단위)
if now - lastRefill >= 60 then
  availableTokens = threshold
  redis.call("SET", tokenKey, tostring(availableTokens))
  redis.call("SET", lastRefillKey, tostring(now))
end
 
-- 6. 진입 가능 여부 판단
if waitingNumber < availableTokens then
  redis.call("ZREM", zqueueKey, userToken)
  redis.call("DECRBY", tokenKey, 1)
  return {
    1,  -- canEnter
    waitingNumber + 1,  -- waiting.number
    0,  -- waiting.estimatedTime
    0   -- waiting.totalCount
  }
end
 
-- 7. 대기 정보 계산
local estimatedTime = math.floor((waitingNumber - availableTokens) * 60 / threshold)
local totalCount = redis.call("ZCARD", zqueueKey)
 
return {
  0,  -- canEnter
  waitingNumber + 1,  -- waiting.number
  estimatedTime,  -- waiting.estimatedTime
  totalCount  -- waiting.totalCount
}
```

---

### API Spec

#### 트래픽 대기 요청 API

- URL : `POST /api/traffic/wait`

##### Request

|    Field    |   Type    |  Length  |     MUC     | Description    |
|:-----------:|:---------:|:--------:|:-----------:|----------------|
|   zoneId    | `String`  |    50    | `MANDATORY` | 트래픽 대기 Zone ID |
|    token    | `String`  |   255    | `OPTIONAL`  | 트래픽 대기 식별 토큰   |
|  clientIp   | `String`  |    50    | `MANDATORY` | 클라이언트 IP 정보    |
| clientAgent | `String`  |    50    | `MANDATORY` | 클라이언트 호출 단말 정보 |

##### Response

|         Field         |   Type    | Length |     MUC     | Description      |
|:---------------------:|:---------:|:------:|:-----------:|------------------|
|       canEnter        | `Boolean` |   -    | `MANDATORY` | 진입 가능 여부         |
|        zoneId         | `String`  |   50   | `MANDATORY` | 트래픽 대기 Zone ID   |
|         token         | `String`  |   50   | `MANDATORY` | 트래픽 대기 식별 토큰     |
|        waiting        | `Object`  |   -    | `OPTIONAL`  | 대기 정보            |
|    waiting.number     | `Number`  |   19   | `MANDATORY` | 현재 대기 순번         |
| waiting.estimatedTime | `Number`  |   19   | `MANDATORY` | 대기 예상 시간         |
|  waiting.totalCount   | `Number`  |   19   | `MANDATORY` | 전체 대기자 수         |
| waiting.pollingPeriod | `Number`  |   19   | `MANDATORY` | 대기 Polling 요청 주기 |
|        result         | `Object`  |   -    | `MANDATORY` | 응답 결과            |
|     result.status     | `String`  |   10   | `MANDATORY` | 응답 결과 상태         |
|      result.code      | `String`  |   11   | `OPTIONAL`  | 에러 코드            |
|    result.message     | `String`  |  255   | `OPTIONAL`  | 에러 메시지           |

#### 트래픽 진입 요청 API

- URL : `POST /api/traffic/entry`

##### Request

| Field  |   Type   | Length |     MUC     | Description    |
|:------:|:--------:|:------:|:-----------:|----------------|
| zoneId | `String` |   50   | `MANDATORY` | 트래픽 대기 Zone ID |
| token  | `String` |  255   | `MANDATORY` | 트래픽 대기 식별 토큰   |

##### Response

|         Field         |   Type    | Length |     MUC     | Description      |
|:---------------------:|:---------:|:------:|:-----------:|------------------|
|       canEnter        | `Boolean` |   -    | `MANDATORY` | 진입 가능 여부         |
|        zoneId         | `String`  |   50   | `MANDATORY` | 트래픽 대기 Zone ID   |
|         token         | `String`  |   50   | `MANDATORY` | 트래픽 대기 식별 토큰     |
|        waiting        | `Object`  |   -    | `OPTIONAL`  | 대기 정보            |
|    waiting.number     | `Number`  |   19   | `MANDATORY` | 현재 대기 순번         |
| waiting.estimatedTime | `Number`  |   19   | `MANDATORY` | 대기 예상 시간         |
|  waiting.totalCount   | `Number`  |   19   | `MANDATORY` | 전체 대기자 수         |
| waiting.pollingPeriod | `Number`  |   19   | `MANDATORY` | 대기 Polling 요청 주기 |
|        result         | `Object`  |   -    | `MANDATORY` | 응답 결과            |
|     result.status     | `String`  |   10   | `MANDATORY` | 응답 결과 상태         |
|      result.code      | `String`  |   11   | `OPTIONAL`  | 에러 코드            |
|    result.message     | `String`  |  255   | `OPTIONAL`  | 에러 메시지           |

---

### Project Structure

```
src/main/kotlin/com/kona/ktc/
├── application/           # Use Cases
│   ├── traffic/
│   │   └── dto/
│   └── config/
├── domain/               # Business Logic
│   ├── model/
│   └── repository/
├── infrastructure/       # External Interfaces
│   ├── redis/
│   └── web/
└── presentation/         # API Layer
    └── dto/
```

---
