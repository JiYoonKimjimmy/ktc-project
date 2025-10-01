# Spring DefaultRedisScript 동작 원리와 최적화

## 개요

Spring Data Redis의 `DefaultRedisScript`는 Redis Lua 스크립트 실행을 자동으로 최적화합니다.
이 문서에서는 `DefaultRedisScript`가 내부적으로 어떻게 동작하며, 어떤 방식으로 네트워크 비용을 최소화하는지 설명합니다.

## DefaultRedisScript란?

`DefaultRedisScript`는 Spring Data Redis에서 제공하는 `RedisScript` 인터페이스의 구현체로, 
Lua 스크립트를 편리하게 실행하고 **자동으로 최적화**해주는 클래스입니다.

### 기본 사용법

```kotlin
// 1. Bean으로 등록
@Bean
fun trafficControlScript(): RedisScript<List<*>> {
    return DefaultRedisScript(
        ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
        List::class.java
    )
}

// 2. 주입받아 사용
@Component
class TrafficControlAdapter(
    private val trafficControlScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter
) {
    suspend fun controlTraffic(keys: List<String>, args: List<String>): List<*> {
        return redisExecuteAdapter.execute(trafficControlScript, keys, args)
    }
}
```

## DefaultRedisScript의 내부 동작 원리

### 1. 스크립트 SHA1 해시 자동 계산

`DefaultRedisScript`는 생성 시점에 스크립트 내용의 SHA1 해시를 자동으로 계산합니다:

```kotlin
class DefaultRedisScript<T> {
    private val scriptAsString: String
    private val sha1: String  // 자동 계산된 SHA1 해시
    
    init {
        // 스크립트 내용으로부터 SHA1 해시 자동 생성
        sha1 = DigestUtils.sha1Hex(scriptAsString)
    }
}
```

### 2. EVALSHA 우선 실행 전략

`DefaultRedisScript`를 사용하면 Spring Data Redis가 다음과 같은 순서로 실행을 시도합니다:

```
1차 시도: EVALSHA <sha1> <keys> <args>
         ↓
    ┌────┴────┐
    │ 성공?   │
    └────┬────┘
         │
    ┌────┼────┐
    YES  │    NO (NOSCRIPT 에러)
    │    │    │
    ↓    │    ↓
  완료   │  2차 시도: EVAL <전체 스크립트> <keys> <args>
         │    │
         │    ↓
         │  Redis가 메모리에 스크립트 캐시
         │    │
         │    ↓
         └─→ 완료
```

### 3. Redis 스크립트 캐시

Redis는 실행된 스크립트를 **서버 메모리에 자동으로 캐싱**합니다:

| 특징 | 설명 |
|------|------|
| **저장 위치** | Redis 서버의 메모리 (RAM) |
| **저장 방식** | SHA1 해시를 키로 사용 |
| **영구성** | ❌ **휘발성** - Redis 재시작 시 삭제 |
| **지속성** | RDB/AOF 백업에 포함되지 않음 |
| **클러스터** | 노드별 독립적인 캐시 |

### 4. 자동 Fallback 메커니즘

```kotlin
// 개발자가 작성하는 코드 (간단!)
redisTemplate.execute(trafficControlScript, keys, args)

// DefaultRedisScript가 내부적으로 처리
try {
    // 1차 시도: EVALSHA (SHA1 해시만 전송 - 40바이트)
    result = redis.evalSha(sha1, keys, args)
} catch (e: RedisNoScriptException) {
    // 2차 시도: EVAL (전체 스크립트 전송 - 수 KB)
    result = redis.eval(scriptAsString, keys, args)
    // Redis가 자동으로 캐시에 저장
}
return result
```

## DefaultRedisScript 사용의 장점

### 1. 개발자 친화적

개발자는 복잡한 최적화 로직을 신경 쓸 필요가 없습니다:

```kotlin
// ❌ 직접 구현하면 복잡함
fun executeScript(script: String, keys: List<String>, args: List<String>): Any {
    val sha1 = DigestUtils.sha1Hex(script)
    return try {
        redis.evalSha(sha1, keys, args)  // EVALSHA 시도
    } catch (e: RedisNoScriptException) {
        redis.eval(script, keys, args)   // EVAL로 Fallback
    }
}

// ✅ DefaultRedisScript 사용 - 간단!
val result = redisTemplate.execute(trafficControlScript, keys, args)
```

### 2. 자동 네트워크 최적화

스크립트가 길수록 네트워크 비용 절감 효과가 큽니다:

```kotlin
// traffic-control.lua: 약 2,700바이트
val script = DefaultRedisScript<List<*>>(scriptContent, List::class.java)

// 첫 요청: EVALSHA (40바이트) 시도 -> NOSCRIPT -> EVAL (2,700바이트)
redisTemplate.execute(script, keys, args)

// 이후 모든 요청: EVALSHA (40바이트)만 전송 ✅
redisTemplate.execute(script, keys, args)  // ~98.5% 네트워크 절감!
```

### 3. Bean 재사용으로 SHA1 계산 최소화

```kotlin
// ✅ 권장: Bean으로 등록 (SHA1 계산 1회)
@Bean
fun trafficControlScript(): RedisScript<List<*>> {
    return DefaultRedisScript(scriptContent, List::class.java)
    // SHA1 계산: 애플리케이션 시작 시 1회만
}

// ❌ 비권장: 매번 새 인스턴스 생성
fun controlTraffic() {
    val script = DefaultRedisScript(scriptContent, List::class.java)
    // SHA1 계산: 매 요청마다! (불필요한 CPU 소비)
}
```

## 추가 최적화: 사전 로드 (Preloading)

`DefaultRedisScript`의 자동 Fallback만으로도 충분하지만, 첫 요청부터 최적화하고 싶다면 사전 로드를 구현할 수 있습니다:

```kotlin
@Configuration
class KtcApplicationConfig(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun trafficControlScript(): RedisScript<List<*>> {
        return DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
            List::class.java
        )
    }

    /**
     * DefaultRedisScript의 자동 최적화 + 사전 로드
     * - 첫 요청에서 NOSCRIPT 에러 방지
     * - 모든 요청이 처음부터 EVALSHA로 실행
     */
    @PostConstruct
    fun preloadRedisScripts() = runBlocking {
        try {
            val script = trafficControlScript()
            val sha1 = reactiveStringRedisTemplate.execute {
                it.scriptingCommands().scriptLoad(script.scriptAsString.toByteArray())
            }.next().block()
            
            logger.info("Redis Lua script preloaded. SHA1: $sha1")
            logger.info("All subsequent requests will use EVALSHA (40 bytes)")
        } catch (e: Exception) {
            logger.warn("Failed to preload script: ${e.message}")
            logger.warn("Will fallback to EVAL on first request (auto-recovered)")
        }
    }
}
```

### 사전 로드의 효과

| 시나리오 | 사전 로드 없음 | 사전 로드 있음 |
|---------|--------------|--------------|
| **첫 요청** | EVALSHA → NOSCRIPT → EVAL | EVALSHA → 성공 ✅ |
| **첫 요청 네트워크** | 40바이트 + 2,700바이트 | 40바이트 |
| **이후 요청** | EVALSHA (40바이트) | EVALSHA (40바이트) |
| **초기 레이턴시** | 약간 높음 | 최소화 ✅ |

### 사전 로드가 특히 유용한 경우

1. **첫 요청의 레이턴시가 중요한 경우**
   - 사용자에게 노출되는 첫 API 호출
   - Health check 등의 초기 검증

2. **Redis 재시작 직후**
   - 스크립트 캐시가 모두 삭제됨
   - 사전 로드가 없으면 첫 요청에서 EVAL 발생

3. **Redis 클러스터 환경**
   - 노드마다 독립적인 캐시
   - 새 노드로 요청 시 NOSCRIPT 발생 가능
   - 사전 로드로 모든 노드에 미리 등록 (구현 필요)

## DefaultRedisScript의 성능 효과

### 네트워크 비용 절감 (traffic-control.lua: 2,700바이트 기준)

| 실행 방식 | 네트워크 전송량 | DefaultRedisScript 동작 |
|----------|----------------|----------------------|
| **직접 EVAL** | 2,700바이트/요청 | 사용하지 않음 ❌ |
| **DefaultRedisScript 첫 요청** | 40 + 2,700 = 2,740바이트 | EVALSHA → EVAL (자동 Fallback) |
| **DefaultRedisScript 이후** | 40바이트/요청 | EVALSHA ✅ |
| **절감률** | **98.5%** | |

### TPS가 높을수록 효과가 큼

```kotlin
// 시나리오: TPS 1,000인 트래픽 제어 시스템

// ❌ DefaultRedisScript 미사용 (항상 EVAL)
네트워크 사용량 = 2,700바이트 × 1,000 TPS = 2.7MB/s

// ✅ DefaultRedisScript 사용 (자동 EVALSHA)
네트워크 사용량 = 40바이트 × 1,000 TPS = 40KB/s

// 절감량: 2.66MB/s (약 67배 감소!)
```

### CPU 비용 절감 (Bean 등록 방식)

```kotlin
// ❌ Bean 미등록: SHA1 계산 매 요청마다
fun controlTraffic() {
    val script = DefaultRedisScript(scriptContent, List::class.java)  
    // SHA1 계산: 매번!
}
// TPS 1,000 → SHA1 계산 1,000회/s

// ✅ Bean 등록: SHA1 계산 1회만
@Bean
fun trafficControlScript() = DefaultRedisScript(scriptContent, List::class.java)
// SHA1 계산: 애플리케이션 시작 시 1회만!
```

## DefaultRedisScript의 자동 복구 메커니즘

### Redis 재시작 시 DefaultRedisScript의 동작

Redis는 스크립트를 메모리에만 캐싱하므로, 재시작 시 모든 캐시가 사라집니다. 
하지만 **DefaultRedisScript는 이를 자동으로 복구**합니다:

```kotlin
// [시점 1] 애플리케이션 시작 + 사전 로드
@PostConstruct
fun preloadScripts() {
    scriptLoad(trafficControlScript)  
    // Redis 메모리에 스크립트 캐시 ✅
}

// [시점 2] 정상 운영 (1시간 동안)
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript 내부 동작:
// 1. EVALSHA <sha1> 실행
// 2. 성공 ✅ (40바이트만 전송)

// [시점 3] Redis 서버 재시작 (운영 팀에서 유지보수)
// 영향:
// - 데이터: RDB로 복구 ✅
// - 스크립트 캐시: 모두 삭제됨 ❌

// [시점 4] 재시작 직후 첫 요청
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript 자동 복구:
// 1. EVALSHA <sha1> 시도
// 2. NOSCRIPT 에러 발생 (캐시에 없음)
// 3. 자동으로 EVAL <전체 스크립트> 실행 ✅
// 4. Redis가 다시 메모리에 캐시
// 5. 요청 정상 완료 ✅

// [시점 5] 이후 모든 요청
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript:
// 1. EVALSHA <sha1> 실행
// 2. 성공 ✅ (캐시가 복구됨)
```

### 캐시 삭제 상황과 DefaultRedisScript의 대응

| 상황 | 원인 | DefaultRedisScript 동작 | 영향 |
|-----|------|----------------------|------|
| **Redis 재시작** | 메모리 초기화 | 자동 Fallback → 복구 | 첫 요청만 느림 |
| **SCRIPT FLUSH** | 관리자 명령 | 자동 Fallback → 복구 | 첫 요청만 느림 |
| **새 클러스터 노드** | 노드별 독립 캐시 | 자동 Fallback → 복구 | 노드당 첫 요청만 느림 |

### 개발자가 할 일: 없음! 🎉

```kotlin
// DefaultRedisScript를 사용하면:
// ✅ Redis 재시작 감지 - 자동
// ✅ 캐시 미스 처리 - 자동
// ✅ 스크립트 재로드 - 자동
// ✅ 에러 복구 - 자동

// 개발자는 그냥 사용하면 됨!
val result = redisTemplate.execute(trafficControlScript, keys, args)
```

### Redis가 스크립트를 영구 저장하지 않는 이유

Redis가 의도적으로 캐시를 휘발성으로 유지하는 이유:

| 이유 | 설명 | DefaultRedisScript의 대응 |
|------|------|------------------------|
| **보안** | 임의의 Lua 코드 영구 저장 위험 | 애플리케이션 코드로 관리 |
| **버전 관리** | 배포 시 스크립트 변경 가능 | 자동 Fallback으로 새 버전 로드 |
| **메모리 효율** | 미사용 스크립트 자동 제거 | 필요 시 자동으로 재로드 |
| **클러스터 일관성** | 노드 간 동기화 불필요 | 각 노드에서 자동 로드 |

```kotlin
// 스크립트 버전 변경 시나리오
// v1.0 배포 -> Redis에 캐시 -> v2.0 배포

// 만약 Redis가 영구 저장한다면:
// 1. v2.0 배포
// 2. SHA1 해시가 변경됨 (스크립트 내용이 다르므로)
// 3. EVALSHA로 v1.0 실행 시도 -> 해시 불일치
// 4. 결국 EVAL로 v2.0 전송해야 함
// 5. 영구 저장의 의미가 없어짐

// DefaultRedisScript 방식:
// 1. v2.0 배포
// 2. 새 SHA1 해시로 EVALSHA 시도
// 3. NOSCRIPT (당연히 Redis에 없음)
// 4. EVAL로 v2.0 전송
// 5. Redis에 v2.0 캐시
// 6. 깔끔하게 버전 전환 완료 ✅
```

## DefaultRedisScript와 Redis 클러스터

### 클러스터 환경의 특징

Redis 클러스터에서는 각 노드가 **독립적인 스크립트 캐시**를 유지합니다:

```kotlin
// Redis 클러스터 환경
// - 노드 A: 슬롯 0-5460
// - 노드 B: 슬롯 5461-10922
// - 노드 C: 슬롯 10923-16383

// 요청 1: traffic:zone:1 (해시 슬롯 -> 노드 A로 라우팅)
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript:
// 1. 노드 A로 EVALSHA
// 2. NOSCRIPT (노드 A에 캐시 없음)
// 3. 노드 A로 EVAL
// 4. 노드 A에 캐시 저장 ✅

// 요청 2: traffic:zone:2 (해시 슬롯 -> 노드 B로 라우팅)
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript:
// 1. 노드 B로 EVALSHA
// 2. NOSCRIPT (노드 B에 캐시 없음)
// 3. 노드 B로 EVAL
// 4. 노드 B에 캐시 저장 ✅

// 요청 3: traffic:zone:1 (다시 노드 A)
redisTemplate.execute(trafficControlScript, keys, args)
// DefaultRedisScript:
// 1. 노드 A로 EVALSHA
// 2. 성공 ✅ (노드 A 캐시 hit)
```

### DefaultRedisScript가 자동으로 처리하는 시나리오

| 시나리오 | DefaultRedisScript 동작 | 개발자 액션 |
|---------|----------------------|----------|
| **새 노드 추가** | 첫 요청 시 EVAL → 캐시 | 없음 (자동) |
| **노드 재시작** | 첫 요청 시 EVAL → 캐시 | 없음 (자동) |
| **슬롯 재분배** | 새 노드에서 EVAL → 캐시 | 없음 (자동) |
| **Failover** | 새 마스터에서 EVAL → 캐시 | 없음 (자동) |

### 클러스터 환경 모니터링

```kotlin
// NOSCRIPT 발생 빈도를 모니터링하면 클러스터 상태 파악 가능

// 정상 상태:
// - 애플리케이션 시작 직후: 노드 수만큼 NOSCRIPT 발생 (정상)
// - 정상 운영 중: NOSCRIPT 거의 없음

// 비정상 상태 (주의 필요):
// - 지속적인 NOSCRIPT: 노드가 계속 재시작되고 있음
// - 특정 키에서만 NOSCRIPT: 슬롯 재분배 진행 중
// - 모든 요청에서 NOSCRIPT: 클러스터 전체 문제

// DefaultRedisScript는 모든 경우를 자동으로 처리하지만,
// NOSCRIPT 빈도가 높다면 클러스터 상태를 점검해야 함
```

## DefaultRedisScript 사용 가이드 (Best Practices)

### 1. 반드시 Bean으로 등록

```kotlin
// ✅ 권장: Bean 등록 (SHA1 계산 1회, 자동 최적화)
@Configuration
class RedisConfig {
    @Bean
    fun trafficControlScript(): RedisScript<List<*>> {
        return DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
            List::class.java
        )
    }
}

// ❌ 비권장: 매번 인스턴스 생성
class TrafficAdapter {
    fun control() {
        val script = DefaultRedisScript(scriptContent, List::class.java)
        // 문제점:
        // 1. 매번 SHA1 계산 (CPU 낭비)
        // 2. DefaultRedisScript의 최적화 효과 감소
    }
}
```

### 2. 스크립트는 별도 파일로 분리

```kotlin
// ✅ 권장: 외부 파일로 관리
@Bean
fun script(): RedisScript<List<*>> {
    return DefaultRedisScript(
        ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
        List::class.java
    )
}
// 장점:
// - IDE의 Lua 문법 하이라이팅
// - 버전 관리 용이
// - 코드와 로직 분리

// ❌ 비권장: 코드에 하드코딩
@Bean
fun script(): RedisScript<List<*>> {
    val scriptContent = """
        local key = KEYS[1]
        local value = ARGV[1]
        return redis.call('set', key, value)
    """.trimIndent()
    return DefaultRedisScript(scriptContent, List::class.java)
}
// 단점:
// - 가독성 저하
// - 유지보수 어려움
// - 문법 검증 불가
```

### 3. 반환 타입 명시

```kotlin
// ✅ 권장: 정확한 반환 타입 지정
@Bean
fun trafficControlScript(): RedisScript<List<*>> {
    return DefaultRedisScript(scriptContent, List::class.java)
    // Lua 스크립트가 List를 반환한다는 것을 명시
}

// ✅ 반환 타입에 따른 예시
@Bean
fun getLongScript(): RedisScript<Long> {
    return DefaultRedisScript(scriptContent, Long::class.java)
}

@Bean
fun getBooleanScript(): RedisScript<Boolean> {
    return DefaultRedisScript(scriptContent, Boolean::class.java)
}

// ❌ 비권장: Any 타입
@Bean
fun script(): RedisScript<Any> {
    return DefaultRedisScript(scriptContent, Any::class.java)
    // 타입 안정성 저하
}
```

### 4. 선택사항: 사전 로드

```kotlin
// 선택사항: 첫 요청 레이턴시가 중요한 경우에만
@PostConstruct
fun preloadRedisScripts() = runBlocking {
    try {
        val script = trafficControlScript()
        val sha1 = reactiveStringRedisTemplate.execute {
            it.scriptingCommands().scriptLoad(script.scriptAsString.toByteArray())
        }.next().block()
        logger.info("Script preloaded: $sha1")
    } catch (e: Exception) {
        logger.warn("Preload failed, will auto-fallback: ${e.message}")
    }
}

// 대부분의 경우 사전 로드 없이도 충분합니다!
// DefaultRedisScript의 자동 Fallback이 있기 때문입니다.
```

### 5. RedisTemplate과 함께 사용

```kotlin
// ✅ 권장: RedisTemplate의 execute 메서드 사용
@Component
class TrafficControlAdapter(
    private val trafficControlScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter  // 내부에서 RedisTemplate 사용
) {
    suspend fun control(keys: List<String>, args: List<String>): List<*> {
        return redisExecuteAdapter.execute(trafficControlScript, keys, args)
        // DefaultRedisScript가 자동으로:
        // 1. EVALSHA 시도
        // 2. 실패 시 EVAL
        // 3. 결과 반환
    }
}

// ❌ 비권장: 직접 Redis 명령 실행
suspend fun control() {
    val scriptContent = "..." // 스크립트 내용
    val result = redis.eval(scriptContent, keys, args)
    // 문제점:
    // - DefaultRedisScript의 최적화 없음
    // - 매번 전체 스크립트 전송
    // - EVALSHA 미사용
}
```

## 요약

### DefaultRedisScript를 사용해야 하는 이유

| 항목 | DefaultRedisScript 사용 | 직접 구현 |
|-----|----------------------|---------|
| **개발 복잡도** | 간단 (Bean 등록만) | 복잡 (Fallback 로직 필요) |
| **네트워크 최적화** | 자동 (EVALSHA) | 수동 구현 필요 |
| **SHA1 계산** | 1회 (Bean 생성 시) | 매번 또는 캐싱 필요 |
| **Redis 재시작 대응** | 자동 복구 | 수동 처리 필요 |
| **클러스터 환경** | 자동 대응 | 노드별 수동 관리 |
| **에러 처리** | 자동 Fallback | try-catch 필요 |

### 핵심 동작 흐름

```
애플리케이션 시작
    ↓
DefaultRedisScript Bean 생성 (SHA1 계산)
    ↓
(선택) @PostConstruct에서 스크립트 사전 로드
    ↓
=== 첫 요청 ===
    ↓
redisTemplate.execute(script, keys, args)
    ↓
DefaultRedisScript: EVALSHA <sha1> 시도
    ↓
Redis: NOSCRIPT 에러 (캐시에 없음)
    ↓
DefaultRedisScript: 자동으로 EVAL <전체 스크립트> 실행
    ↓
Redis: 메모리에 스크립트 캐시 저장
    ↓
결과 반환 ✅
    ↓
=== 이후 모든 요청 ===
    ↓
DefaultRedisScript: EVALSHA <sha1> 실행
    ↓
Redis: 캐시 hit → 즉시 실행
    ↓
결과 반환 ✅ (네트워크 98.5% 절감!)
```

### 실제 프로젝트 적용 예시

```kotlin
// 1. Configuration에서 Bean 등록
@Configuration
class KtcApplicationConfig {
    @Bean
    fun trafficControlScript(): RedisScript<List<*>> {
        return DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
            List::class.java
        )
    }
}

// 2. Adapter에서 주입받아 사용
@Component
class TrafficControlScriptExecuteAdapter(
    private val trafficControlScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficControlPort {
    
    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val keys = TrafficCacheKey.getTrafficControlKeys(traffic.zoneId).map { it.value }
        val args = listOf(traffic.token, now.toEpochMilli().toString(), defaultThreshold)
        
        // DefaultRedisScript가 알아서 최적화!
        val (result, number, estimatedTime, totalCount) = 
            redisExecuteAdapter.execute(trafficControlScript, keys, args).map { it as Long }
        
        return TrafficWaiting(result, number, estimatedTime, totalCount)
    }
}
```

## 참고 자료

### Redis 공식 문서
- [Redis EVAL 명령](https://redis.io/commands/eval/)
- [Redis EVALSHA 명령](https://redis.io/commands/evalsha/)
- [Redis SCRIPT LOAD 명령](https://redis.io/commands/script-load/)
- [Redis Lua Scripting](https://redis.io/docs/manual/programmability/eval-intro/)

### Spring Data Redis
- [Spring Data Redis - Scripting](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#scripting)
- [RedisScript API Documentation](https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/script/RedisScript.html)
- [DefaultRedisScript API Documentation](https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/script/DefaultRedisScript.html)

