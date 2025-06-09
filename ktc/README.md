# KTC (Kona Traffic Controller) 😏

## Requirement

- `1분` 단위 최대 허용치`(Threshold)`만큼 트래픽 제한하여 대용량 트래픽 제어
    - Threshold 예상 범위 : `70K` ~ `100K`
- 실시간 or 실시간에 준하는 트래픽 진입 현황 정보 제공
    - HTTP Long-Term Polling 방식
    - HTTP SSEs or WebSockets 방식

---

## Architecture

### 트래픽 대기/진입 프로세스

- 1분당 진입 허용 임계치(`threshold`) 설정 기반 트래픽 제어 처리
  - 1분 트래픽은 6초마다 `ceil(threshold/10)` 만큼 트래픽 진입 처리
- 트래픽 진입 대기 처리 시, 대기 순번 할당 & 대기 예상 시간 계산
- 트래픽 대기 순번은 고유한 `token` 식별자 진입 시점 기준으로 대기 순번 할당
- 트래픽 대기 상태 `token` 은 **반복 요청**을 통해 트래픽 진입 허용 확인

> #### 예상 시나리오
> 
> 1. 분당 `threshold`: 2000 (6초당 200 허용)
>    - 1 ~ 2000번: 즉시 입장 가능 (0분 대기)
>    - 2001 ~ 4000번: 1분 대기 후 입장 
>    - 4001 ~ 6000번: 2분 대기 후 입장
> 2. `threshold` 변경: 2000 > 1000 감소 (6초당 100 허용)
>    - 6001 ~ 7000번: 3분 대기 후 입장
>    - 7001 ~ 8000번: 4분 대기 후 입장
>    - 8001 ~ 9000번: 5분 대기 후 입장
>    - 9001 ~ 10000번: 6분 대기 후 입장
> 3. `threshold` 변경 : 1000 > 500 감소 (6초당 50 허용)
>    - 10001 ~ 10500번: 7분 대기 후 입장
>    - 10501 ~ 11000번: 8분 대기 후 입장
> 4. `threshold` 변경 : 500 > 1000 증가 (6초당 100 허용)
>    - 11001 ~ 12000번: 9분 대기 후 입장

---

### 트래픽 제어 프로세스

1. `Zone` 상태 확인
   - `Zone` 상태 `BLOCKED` 인 경우, 즉시 `result: -1` 반환하여 종료
2. `threshold` 설정 조회
3. 6초 단위 Slot 계산
   - 현재 시간(`nowMillis`) 기준 분(`minute`) 과 6초 단위 Slot(`slot`) 정보 계산
   - `minute = floor(nowMillis / 60000)`
   - `slot = floor((nowMillis % 60000) / 1000) / 6`
4. `token` 대기열 `queue` 등록
5. 현재 `slot` 진입 Count 조회
6. 진입 허용 조건 확인
   - 현재 `slot` 진입 Count < `slot` 허용 트래픽 수(`threshold / 10`)
   - 대기 순번(`rank`) < `slot` 허용 트래픽 수(`threshold / 10`)
7. 진입 허용 여부별 정보 반환 처리
   - 진입 허용인 경우
     - 현재 `slot` 진입 Count 증가
     - 대기열 `queue` & 토큰 마지막 Polling 시간 정보에서 `token` 제거
     - 현재 `slot` 진입 Count 캐시 1분 만료 설정
     - 결과: `{ 1, 0, 0, totalCount }` 반환 처리
   - 진입 대기인 경우
     - 토큰 마지막 Polling 시간 정보 업데이트
     - 예상 대기 시간(`waitTime`) 계산
     - 결과: `{ 0, rank+1, waitTime, totalCount }` 반환 처리

```text
-- thrshold: 10 / tokens: 10
[0ms] token1 요청 → {1, 0, 0, 0}  // 진입 허용
[100ms] token2 요청 → {0, 1, 6000, 1}  // 대기, 2번째 순번, 6초 대기
[200ms] token3 요청 → {0, 2, 12000, 2} // 대기, 3번째 순번, 12초 대기
[300ms] token4 요청 → {0, 3, 18000, 3} // 대기, 4번째 순번, 18초 대기
[400ms] token5 요청 → {0, 4, 24000, 4} // 대기, 5번째 순번, 24초 대기
[500ms] token6 요청 → {0, 5, 30000, 5} // 대기, 6번째 순번, 30초 대기
[600ms] token7 요청 → {0, 6, 36000, 6} // 대기, 7번째 순번, 36초 대기
[700ms] token8 요청 → {0, 7, 42000, 7} // 대기, 8번째 순번, 42초 대기
[800ms] token9 요청 → {0, 8, 48000, 8} // 대기, 9번째 순번, 48초 대기
[900ms] token10 요청 → {0, 9, 54000, 9} // 대기, 10번째 순번, 54초 대기
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
|  clientIP   | `String`  |    50    | `MANDATORY` | 클라이언트 IP 정보    |
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
