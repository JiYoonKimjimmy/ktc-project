# KTC API (Kona Traffic Controller API) 😎

## Requirement

- **KTC 프로젝트** 관련 API 서비스 제공
- 트래픽 제어 Zone 설정 정보 관리
- 트래픽 제어 Zone 현황 실시간 모니터링 서비스 제공
- 트래픽 제어 Zone 대기 통계/집계 조회 서비스 제공
- KTC 백오피스 운영 데이터 관리
  - 운영 관리자 계정 정보 관리 

---

## Architecture

### 주요 도메인 정보

|        Domain        |      설명      |
|:--------------------:|:------------:|
|    `TrafficZone`     |   Zone 정보    |
| `TrafficZoneWaiting` |  Zone 대기 정보  |
| `TrafficZoneMonitor` | Zone 모니터링 정보 |
|  `TrafficZoneGroup`  |  Zone 그룹 정보  |
|       `Member`       |  운영 관리자 정보   |

---

#### `TrafficZone`: Zone 정보

- 트래픽 제어를 위한 `Zone` 설정 정보 관리
- `Zone` 설정 정보 DB 저장 시, 등록/변경 내용 Cache 저장 동시 처리 

##### 구성 요소
- **`zoneId`: Zone 고유 식별 ID**
  - 서비스 화면, API URL 등 트래픽 제어를 위한 `Zone` 구분 정보
  - `zoneId` 생성 방식 2가지 제공
    - 운영 관리자 지정 방식
      - 기존 (구)트레이서 서비스 운영 방식 그대로 적용하여 아래와 같은 형식의 지역화폐 서비스 `Zone` 구성
        - `000140000000000:APP_MAIN`
        - `000140000000000:APP_RECHARGE`
    - 자체 생성 방식: `KZ<SnowflakeID 19자리>`
- **`zoneAlias`: Zone 별칭**
- **`threshold`: Zone 트래픽 제어 임계값**
- **`status`: Zone 상태**
  - `ACTIVE`: 활성화 상태
  - `BLOCKED`:  차단 상태
  - `DELETED`:  삭제 상태
  - `FAULTY_503`: (장애)차단 상태(QA)
    - 운영 서비스에서는 `503` 에러 응답은 `L/B` 에서 차단
- **`activationTime`: Zone 활성화 시간**
  - `Zone` 트래픽 제어 전 활성화 시간 확인하여 현재 시간보다 이후인 경우, 전체 진입 처리

---

#### `TrafficZoneWaiting`: Zone 대기 정보

- `zoneId` 기준 현재 `Zone` 대기 정보 관리
- `zoneId` 기준으로 대기열 Queue(`Sorted-Set`), 진입자 수 Cache 조회

##### 구성 요소

- **`entryCount`: `Zone` 진입자 수**
  - `zoneId` 기준 1일 진입자 수
  - 매일 00시 00분 초기화
- **`waitingCount`: `Zone` 대기자 수**
  - `zoneId` 기준 현재 대기열 Queue 전체 카운트
- **`estimatedClearTime`: `Zone` 트래픽 대기 전체 해소 예상 시간**
  - `waitingCount` 기준 `threshold` 단위로 계산하여 전체 해소 예상 시간 계산

---

#### `TrafficZoneMonitor`: Zone 모니터링 정보

- `zoneId` 기준 5초마다 자체 스케쥴링을 통해 수집된 모니터링 결과 정보 관리
- 스케쥴링으로 수집된 모니터링 결과는 DB 테이블 저장 후 최신 결과는 **Local Cache(`Caffeine`)** 저장

##### 구성 요소

- **`zoneId`: Zone 고유 식별 ID**
- **`zoneAlias`: Zone 별칭**
- **`threshold`: Zone 트래픽 제어 임계값**
- **`status`: Zone 상태**
- **`activationTime`: Zone 활성화 시간**
- **`entryCount`: `Zone` 진입자 수**
- **`waitingCount`: `Zone` 대기자 수**
- **`estimatedClearTime`: `Zone` 트래픽 대기 전체 해소 예상 시간**

---

#### `Member`: 운영 관리자 정보

- KTC 백오피스 서비스 운영 관리자 정보 관리

##### 구성요소

- **`memberId`: 운영 관리자 고유 식별 ID**
- **`loginId`: 로그인 ID**
- **`password`: 비밀번호**
- **`name`: 운영 관리자 이름**
- **`email`: 운영 관리자 업무 이메일**
- **`team`: 운영 관리자 소속 팀 구분**
- **`role`: 운영 관리자 역할**
  - `SUPER_ADMIN`
    - 계정 설정 관리
    - Zone 설정 관리
    - 모니터링/통계 데이터 조회
  - `ADMIN`
    - Zone 설정 관리
    - 모니터링/통계 데이터 조회
  - `MONITOR`
    - 모니터링/통계 데이터 조회
- **`status`: 운영 관리자 상태**
  - `ACTIVE`: 활성화 상태
  - `INACTIVE`: 비활성화 상태
  - `DELETED`:  삭제 상태

---
