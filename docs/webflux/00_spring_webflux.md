# Spring WebFlux 웹소켓 핸들러 개선기

## 1. 문제 상황

### 1.1 기존 코드의 문제점

```kotlin
// 문제가 있는 코드
doOnNext { msg ->
    if (msg.startsWith("PING:")) {
        session.send(Mono.just(session.textMessage("PONG:${msg.replace("PING:", "")}"))).subscribe()
    }
    // ... 다른 메시지 처리
}
```

1. **리액티브 스트림 중단**
   - `subscribe()`를 직접 호출함으로써 리액티브 체인이 중단됨
   - 에러 처리와 리소스 정리가 보장되지 않음
   - 백프레셔(backpressure) 처리가 불가능

2. **메모리 누수 위험**
   - 구독이 명시적으로 취소되지 않을 수 있음
   - 리소스가 제대로 정리되지 않을 수 있음

3. **에러 처리 미흡**
   - 에러가 발생했을 때 적절한 처리가 어려움
   - 에러 전파가 리액티브 체인을 통해 이루어지지 않음

## 2. 개선 방안

### 2.1 리액티브 체인 통합

```kotlin
// 개선된 코드
val inbound = session.receive()
    .map(WebSocketMessage::getPayloadAsText)
    .flatMap { msg ->
        when {
            msg.startsWith("PING:") -> {
                session.send(Mono.just(session.textMessage("PONG:${msg.replace("PING:", "")}")))
            }
            // ... 다른 메시지 처리
        }
    }
    .thenMany(Flux.never<Void>())
```

### 2.2 개선된 점

1. **리액티브 스트림 통합**
   - 모든 웹소켓 통신이 하나의 리액티브 체인으로 통합
   - `flatMap`을 사용하여 비동기 작업을 체인 내에서 처리
   - 에러 처리와 리소스 정리가 일관되게 이루어짐

2. **코드 가독성 향상**
   - `when` 표현식을 사용하여 메시지 타입별 처리를 명확하게 구분
   - 각 메시지 처리 로직이 독립적으로 분리되어 유지보수가 용이

3. **에러 처리 개선**
   - 리액티브 체인을 통한 에러 전파
   - 각 연산자에서 발생하는 에러를 적절히 처리 가능

## 3. Spring WebFlux 모범 사례

### 3.1 리액티브 프로그래밍 원칙

1. **구독(Subscribe) 직접 호출 지양**
   - 리액티브 체인을 유지하고 최종 구독은 컨테이너에 위임
   - 사이드 이펙트는 `doOnNext`, `doOnError` 등을 활용

2. **에러 처리**
   - `onErrorResume`, `onErrorReturn` 등을 사용한 에러 처리
   - 체인 내에서 에러를 적절히 처리하고 전파

3. **리소스 관리**
   - `using` 연산자를 활용한 리소스 자동 정리
   - 명시적인 구독 취소 처리

### 3.2 WebSocket 핸들러 구현 시 주의사항
1. **세션 생명주기 관리**
   - WebSocket 세션의 연결/종료를 적절히 처리
   - 세션 상태에 따른 리소스 정리

2. **메시지 처리**
   - 메시지 타입별 처리 로직을 명확하게 구분
   - 비동기 작업을 리액티브 체인 내에서 처리

3. **백프레셔 처리**
   - `buffer` 연산자 등을 활용한 백프레셔 처리
   - 시스템 부하에 따른 적절한 처리

## 4. 결론

Spring WebFlux를 사용할 때는 리액티브 프로그래밍의 원칙을 잘 지키는 것이 중요합니다. 특히 `subscribe()`를 직접 호출하는 것은 피하고, 리액티브 체인을 유지하면서 비동기 작업을 처리하는 것이 좋습니다. 이를 통해 더 안정적이고 유지보수가 용이한 코드를 작성할 수 있습니다.
