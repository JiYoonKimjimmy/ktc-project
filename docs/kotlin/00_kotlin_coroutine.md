# Coroutine with Kotlin (Part.1)

## CoroutineScope(Dispatchers.IO) + SupervisorJob

```kotlin
private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
```

### CoroutineScope

- `CoroutineScope` 코루틴의 범위를 정의하는 인터페이스
- 코루틴을 사용할 때, 적절한 scope 선택하여 리소스 누수 방지 목적
- 코루틴간 구조화된 동시성 관리 필요

#### 주요 특징

- 코루틴 생명주기 관리
- 코루틴의 실행 대상 컨텍스트 정의
- 부모-자식 관계의 구조화된 동시성 제공
- `scope` 안 예외 발생 or 취소 시, `score` 안의 모든 코루틴 취소

#### 사용 예시

1. `GlobalScope`: 애플리케이션 수명 주기와 동일한 전역 scope (권장되지 않음)
2. `lifecycleScope`: `Android` 생명주기와 연결된 scope
3. `viewModelScope`: `ViewModel` 생명주기와 연결된 scope
4. `coroutineScope`: **사용자 정의 scope**

### SupervisorJob

- 자식 코루틴의 실패가 다른 자식이나 부모에게 전파되지 않도록 하는 특별한 Job
- 일반 Job과 달리 자식 코루틴의 예외를 부모로 전파하지 않음
- 각 자식 코루틴이 독립적으로 실패할 수 있도록 허용

#### SupervisorJob 주요 특징

- 자식 코루틴 실패 격리
- 다른 자식 코루틴에 영향 없이 개별 실패 처리 가능
- 부모 코루틴은 계속 실행
- 실패한 자식 코루틴만 취소

#### SupervisorJob 사용 예시

- 여러 독립적인 작업을 병렬 실행 필요한 경우
- 일부 작업의 실패가 전체 시스템 영향 없어야하는 경우
- 각 작업의 실패를 개별적으로 처리 필요한 경우
- 서버에서 여러 클라이언트 요청 처리 필요한 경우

### CoroutineScope vs runBlocking

#### `runBlocking` 문제점

```kotlin
@EventListener
fun handleSaveTrafficStatusEvent(event: SaveTrafficStatusEvent) = runBlocking {
    messagePublisher.publishDirectMessage(...)
}
```

- 현재 스레드를 블로킹하여 작업 진행
- `handleSaveTrafficStatusEvent` 스레드가 메시지 발행하여 완료될때까지 대기
- 대량 이벤트 발생 시 스레드-풀 고갈 위험
- 비동기 처리의 장점 효과 미비

#### `CoroutineScope` 필요성

```kotlin
private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun handleSaveTrafficStatusEvent(event: SaveTrafficStatusEvent) {
    coroutineScope.launch {
        messagePublisher.publishDirectMessage(...)
    }
}
```

- 비동기 실행으로 스레드 블로킹하지 않음
- `handleSaveTrafficStatusEvent` 스레드가 즉시 반환
- 스레드-풀 효율적 관리 가능
- 구조화된 코루틴 동시성 제공

---
