# JVM Option 변경 사항

## 개선 배경

### 1. 프로젝트 특성
- KTC(Kona Traffic Controller)는 대용량 트래픽 제어 시스템
- 1분 단위로 최대 허용치(Threshold)만큼 트래픽 제한
- Threshold 예상 범위: 70K ~ 100K
- 실시간 트래픽 진입 현황 정보 제공

### 2. 기존 설정의 문제점
- **메모리 설정**: 
  - 초기 힙 크기가 작아 동적 증가 시 오버헤드 발생
  - NewRatio 설정으로 인한 불필요한 메모리 조정
- **GC 설정**: 
  - ZGC는 2GB 정도의 힙 크기에서는 오버스펙
  - STW(Stop-The-World) 시간 제한이 없어 예측 불가능한 지연 발생
- **로깅 설정**: 
  - 구버전/신버전 설정 혼용으로 인한 로깅 무효화
  - 불필요한 로그 정보 수집

### 3. 개선 목표
- **안정성**: 
  - 예측 가능한 GC 동작
  - 안정적인 메모리 사용
- **성능**: 
  - 최소한의 GC 오버헤드
  - 효율적인 메모리 활용
- **모니터링**: 
  - 명확한 GC 로그 수집
  - 문제 발생 시 빠른 원인 파악

### 4. 개선 방향
- **현대적인 GC 사용**: 
  - G1GC 도입으로 예측 가능한 GC 동작
  - MaxGCPauseMillis 설정으로 STW 시간 제한
- **메모리 최적화**: 
  - 초기/최대 힙 크기 동일 설정
  - 불필요한 메모리 비율 설정 제거
- **로깅 개선**: 
  - 통합된 로깅 설정
  - 필요한 정보만 선택적 수집

---

### ASIS 설정

```shell
## Configuration ###############################################################
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m -XX:NewRatio=2"
JAVA_OPTS="$JAVA_OPTS -Xss32m"
JAVA_OPTS="$JAVA_OPTS -XX:SurvivorRatio=4"
JAVA_OPTS="$JAVA_OPTS -XX:+UseZGC"
JAVA_OPTS="$JAVA_OPTS -Xlog:safepoint=debug"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
JAVA_OPTS="$JAVA_OPTS -verbose:gc -Xloggc:$GCLOG"
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*"
JAVA_OPTS="$JAVA_OPTS -Xlog:gc+heap=debug"
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=$GCDIR/$SERVICE_NAME-java_pid.hprof"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF8"
```

---

### TOBE 설정

```shell
## Configuration ###############################################################
# 1. 힙 메모리 설정 변경
#JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m -XX:NewRatio=2"
JAVA_OPTS="$JAVA_OPTS -Xmx2048m -Xmx2048m"

# 2. 스레드 스택 크기 설정 제거
# JAVA_OPTS="$JAVA_OPTS -Xss32m"

# 3. GC 설정 변경
#JAVA_OPTS="$JAVA_OPTS -XX:+UseZGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 4. GC 로깅 설정 통합
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*=info,gc+heap=debug,safepoint=debug:file=$GCLOG:time,level,tags"
#JAVA_OPTS="$JAVA_OPTS -Xlog:safepoint=debug"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#JAVA_OPTS="$JAVA_OPTS -Xlog:gc*"
#JAVA_OPTS="$JAVA_OPTS -Xlog:gc+heap=debug"
#JAVA_OPTS="$JAVA_OPTS -verbose:gc -Xloggc:$GCLOG"

JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=$GCDIR/$SERVICE_NAME-java_pid.hprof"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF8"
```

---

### 변경 사항 정리

#### 1. 힙 메모리 설정 변경
- **ASIS**: `-Xms512m -Xmx2048m -XX:NewRatio=2`
- **TOBE**: `-Xmx2048m -Xmx2048m`
- **변경 이유**: 
  - 하나의 VM에 하나의 Java 인스턴스만 실행되므로 초기 힙 크기를 낮게 설정할 필요가 없음
  - 동적 힙 증가 시 발생하는 오버헤드 방지
  - G1GC는 New/Old 비율을 자동으로 조절하므로 NewRatio 설정 불필요

#### 2. 스레드 스택 크기 설정 제거
- **ASIS**: `-Xss32m`
- **TOBE**: 제거 (기본값 사용)
- **변경 이유**:
  - 스레드당 32MB의 큰 스택 메모리는 리소스 낭비
  - Virtual Thread 사용 시 더욱 위험한 설정
  - 기본값 사용이 더 안전하고 효율적

#### 3. GC 설정 변경
- **ASIS**: `-XX:+UseZGC`
- **TOBE**: `-XX:+UseG1GC -XX:MaxGCPauseMillis=200`
- **변경 이유**:
  - ZGC는 수십~수백GB의 대용량 힙에서 적합
  - 2GB 정도의 힙 크기에서는 G1GC가 더 효율적
  - MaxGCPauseMillis로 STW 시간 제한 설정

#### 4. GC 로깅 설정 통합
- **ASIS**: 여러 개의 구버전 GC 로깅 설정 혼용
- **TOBE**: `-Xlog:gc*=info,gc+heap=debug,safepoint=debug:file=$GCLOG:time,level,tags`
- **변경 이유**:
  - 구버전/신버전 설정 혼용 시 신버전 설정이 무효화될 수 있음
  - 통합된 로깅 설정으로 관리 용이성 향상
  - 필요한 로그 레벨과 정보만 선택적으로 수집

#### 5. 유지된 설정
- `-XX:+HeapDumpOnOutOfMemoryError`
- `-XX:HeapDumpPath=$GCDIR/$SERVICE_NAME-java_pid.hprof`
- `-Dfile.encoding=UTF8`

---

## logback.xml > logback-spring.xml

### ASIS

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds">

    <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/home/ktc/log/${USER}-${HOSTNAME}-${INSTANCE_NAME}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>/home/ktc/log/${USER}-${HOSTNAME}-${INSTANCE_NAME}-%d{yyyyMMddHH}-%i.log
            </fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
        </rollingPolicy>

        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS}.%thread> %-5level T[%X{correlationId}] U[%X{userId}] M[%X{mpaId}] - %msg%n
            </pattern>
        </encoder>
    </appender>

    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/home/ktc/log/${USER}-${HOSTNAME}-${INSTANCE_NAME}-ERROR.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>/home/ktc/log/${USER}-${HOSTNAME}-${INSTANCE_NAME}-ERROR-%d{yyyyMMddHH}-%i.log
            </fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
        </rollingPolicy>

        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>

        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS}.%thread> %-5level T[%X{correlationId}] U[%X{userId}] M[%X{mpaId}] - %msg%n
            </pattern>
        </encoder>
    </appender>

    <logger name="org.springframework" level="WARN"/>
    <logger name="org.hibernate" level="WARN"/>
    <logger name="io.netty" level="WARN"/>
    <logger name="org.aspectj.weaver" level="WARN"/>


    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
</configuration>
```

---

### TOBE

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds">

    <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>

    <springProperty scope="context" name="logPath" source="logging.file.path" defaultValue="/home/ktc/log"/>
    <springProperty scope="context" name="logFileName" source="logging.file.name" defaultValue="${USER}-${HOSTNAME}-${INSTANCE_NAME}"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${logPath}/${logFileName}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${logPath}/${logFileName}-%d{yyyyMMddHH}-%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS}.%thread> %-5level T[%X{correlationId}] U[%X{userId}] M[%X{mpaId}] - %msg%n
            </pattern>
        </encoder>
    </appender>

    <springProfile name="dev">
        <logger name="com.kona.ktc" level="DEBUG"/>
        <logger name="org.springframework" level="INFO"/>
        <logger name="org.springframework.data.redis" level="DEBUG"/>
        <logger name="org.springframework.web" level="DEBUG"/>
        <logger name="org.springframework.amqp" level="DEBUG"/>
        <logger name="org.springframework.boot.actuator" level="INFO"/>
        <logger name="io.netty" level="INFO"/>
        <logger name="kotlinx.coroutines" level="INFO"/>
    </springProfile>

    <springProfile name="qa">
        <logger name="com.kona.ktc" level="DEBUG"/>
        <logger name="org.springframework" level="INFO"/>
        <logger name="org.springframework.data.redis" level="DEBUG"/>
        <logger name="org.springframework.web" level="DEBUG"/>
        <logger name="org.springframework.amqp" level="DEBUG"/>
        <logger name="org.springframework.boot.actuator" level="INFO"/>
        <logger name="io.netty" level="INFO"/>
        <logger name="kotlinx.coroutines" level="INFO"/>
    </springProfile>

    <springProfile name="prod">
        <logger name="com.kona.ktc" level="INFO"/>
        <logger name="org.springframework" level="WARN"/>
        <logger name="org.springframework.data.redis" level="WARN"/>
        <logger name="org.springframework.web" level="WARN"/>
        <logger name="org.springframework.amqp" level="WARN"/>
        <logger name="org.springframework.boot.actuator" level="WARN"/>
        <logger name="io.netty" level="WARN"/>
        <logger name="kotlinx.coroutines" level="WARN"/>
    </springProfile>

    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
</configuration>
```

---

### 1. Spring Boot 프로파일 지원
- **ASIS**: 환경별 로깅 레벨 설정 불가
- **TOBE**: `springProfile` 태그를 사용하여 환경별 로깅 레벨 설정
- **변경 이유**:
  - dev, qa, prod 환경에 따라 다른 로깅 레벨 적용
  - 개발 환경에서는 더 상세한 로그, 운영 환경에서는 필요한 로그만 출력

### 2. Spring Boot 프로퍼티 사용
- **ASIS**: 하드코딩된 로그 경로와 파일명
- **TOBE**: `springProperty` 태그를 사용하여 외부 설정으로 분리
- **변경 이유**:
  - `application.properties`나 `application.yml`에서 설정 가능
  - 환경별로 다른 로그 경로와 파일명 설정 가능

### 3. 로그 파일 관리 개선
- **ASIS**: 로그 파일 보관 기간과 크기 제한 없음
- **TOBE**: 
  - `maxHistory`: 30일 동안의 로그 파일 보관
  - `totalSizeCap`: 전체 로그 파일 크기 제한 (3GB)
- **변경 이유**:
  - 디스크 공간 효율적 사용
  - 로그 파일 관리 자동화

### 4. 로깅 레벨 세분화
- **ASIS**: 모든 환경에서 동일한 로깅 레벨
- **TOBE**: 환경별로 다른 로깅 레벨 설정
  - dev, qa: DEBUG/INFO 레벨
  - prod: INFO/WARN 레벨
- **변경 이유**:
  - 개발 환경에서는 상세한 로그로 디버깅 용이
  - 운영 환경에서는 필요한 로그만 출력하여 성능 최적화

### 5. 라이브러리 로그 레벨 최적화
- **ASIS**: 
  - `org.springframework`: WARN
  - `org.hibernate`: WARN
  - `io.netty`: WARN
  - `org.aspectj.weaver`: WARN
- **TOBE**: 
  - `org.springframework`: INFO/WARN
  - `