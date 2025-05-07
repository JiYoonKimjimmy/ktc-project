# KTC-Project (Kona Traffic Controller) 😎

## Summary

- 플랫폼 진입 트래픽 제어 목적 **가상 순번 대기(Virtual Waiting Room)** 자체 솔루션
- 일시적으로 폭증하는 대용량 트래픽 제어
- 트래픽 제어, 분석, 통계 등 서비스 진입 요청 관련 트래픽 정보 관리

### Project Feature

- `Zone` 설정 기준 트래픽 제어
  - `Zone` : 트래픽 제어 대상 or 영역 (e.g. 메인화면 진입, 충전 요청, 등등..)
  - `Zone` 별 `분당 서비스 진입 허용 수` 를 `Threshold` 임계치 기준으로 설정하여 트래픽 제어
- 실시간 트래픽 대기 현황 모니터링 기능 제공
- 실시간 `Zone` 별 `Threshold` 제어 기능 제공
- 트래픽 제어 관련 통계/집계 데이터 제공

---

### Project Module Structure

```
ktc-project
  ├─common : DTO, Util 등 공통 코드 관리 모듈
  ├─ktc    : (Kona Traffic Controller) 서비스 진입 트래픽 제어 처리 모듈
  └─ktca   : (Kona Traffic Controller Api) 트래픽 정보 제공 관리 모듈
```

---

### Project Infra Structure

- `TODO`

---

### Project Environment

- JDK 21
- Kotlin 1.9.25
- Spring Boot 3.4.4
- gradle 8.13
- Spring Web MVC (not Webflux)
- Virtual Thread
- Tomcat

#### Configuration

| 구분                          | ktc     | ktca    |
|-----------------------------|---------|:--------|
| Context path                | `/ktc`  | `/ktca` |
| API port                    | `21000` | `21002` |
| Management port (/actuator) | `21001` | `21003` |

- Graceful shutdown
- ktc - Basic Redis (need to custom)
- `No KSL library` -> Applied only necessary customizations

---

### How To development & Deploy

- `Git Basic` or `Git-Flow` 방식으로 Git Branch 관리

> #### Git Flow
> 
> Git Flow 시작 전 설치 및 `git flow init` 초기화 필수 실행

#### Start Feature

```bash
# Git Basic
# 1. `develop` 브랜치 > `feature/<branch>` 브랜치 생성
$ git branch feature/<branch> develop
# 2. 작업 완료 `feature/<branch>` 브랜치 > `develop` 브랜치 병합
$ git switch develop
$ git merge <브랜치명>

# Git Flow
# 1. `develop` 브랜치 > `feature/<branch>` 브랜치 생성
$ git flow feature start <브랜치명>
# 2. 작업 완료 `feature/<branch>` 브랜치 > `develop` 브랜치 병합
$ git flow feature finish <브랜치명>
```

#### Start Release

##### Change Project Version

```properties
# `ktc` or `ktca` module > gradle.properties
version.primary=4
version.major=81
version.minor=0.00
```

##### Deploy to `DEV1`

```bash
# Git Basic
# 1. `develop` 브랜치 > `release/<module>/develop` 브랜치 병합
$ git switch release/ktc/develop
$ git merge develop
# 2. `release/<module>/develop` 브랜치 PUSH
$ git push origin release/ktc/develop

# Git Flow
# 1. `develop` 브랜치 > `release/<module>/develop` 브랜치 생성
$ git flow release start ktc/develop
# 2. `release/<module>/develop` 브랜치 PUSH
$ git flow release publish ktc/develop
```

##### Deploy to `DEV3`

```bash
# Git Basic
# 1. `develop` 브랜치 > `release/<module>/qa` 브랜치 병합
$ git switch release/ktc/qa
$ git merge develop
# 2. `release/<module>/qa` 브랜치 PUSH
$ git push origin release/ktc/qa

# Git Flow
# 1. `develop` 브랜치 > `release/<module>/qa` 브랜치 생성
$ git flow release start ktc/qa
# 2. `release/<module>/qa` 브랜치 PUSH
$ git flow release publish ktc/qa
```

#### Start Deployment

```bash
# Git Basic
# 1. `release/<module>/qa` 브랜치 > `master` 브랜치 병합
$ git switch master
$ git merge release/ktc/qa
# 2. `master` 브랜치 PUSH
$ git push origin master

# Git Flow
# 1. `release/<module>/qa` 브랜치 > `master` 브랜치 병합
$ git flow release finish release/ktc/qa
# 2. `master` 브랜치 PUSH
$ git push origin master
```

---

### How To verify jacoco

- Note `build.gradle.kts`
- tasks with type `test` ends with the task below 
  - jacocoTestReport -> generate report
  - jacocoTestCoverageVerification -> verification test coverage
- jacoco.xml location -> $projectDir/report/jacoco-${project.name}/jacoco.xml
    - when jenkins build $projectName-ANALYSIS -> upload jacoco.xml to Sonarqube  

```bash
# example
./gradlew ktc:build
# or
./gradlew ktc:test  
```

---

### Logging

- No HTTP REQ, RES Logging

---

### Cautions

- `Do not use Thread-Local object/variable (because of Virtual-Thread)` 

---
