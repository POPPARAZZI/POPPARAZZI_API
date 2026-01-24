# 프로젝트 구조 설명

이 프로젝트는 **API / Domain / Common** 을 명확히 분리한 멀티 모듈 구조로 설계되었습니다.

목표는 다음과 같습니다.

* HTTP 계층과 비즈니스 로직 분리
* 도메인 중심 설계
* 확장 가능한 구조 (batch, chat, push 등 추가 가능)

---

## 전체 모듈 구조

```
root
 ├─ api        : 실행 모듈 (Controller, API DTO)
 ├─ domain     : 비즈니스 로직 + JPA
 └─ common     : 공통 설정 및 인프라
```

의존성 방향은 아래와 같습니다.

```
api  → domain → common
api  → common
```

> domain, common 은 api 를 의존하지 않습니다.

---

## 1. api 모듈

**역할**

* HTTP 요청/응답 처리
* Validation
* API 스펙 관리

**특징**

* 비즈니스 로직 없음
* Entity 직접 사용하지 않음
* domain 서비스만 호출

### 구조 예시

```
api
 └─ com.xxx.api.**
    ├─ controller
    │   └─ MoimController.java
    └─ dto
        ├─ request
        │   └─ CreateMoimRequest.java
        └─ response
```

### 역할 흐름

```
Controller
 → api Request DTO (@Valid)
 → domain Command
 → domain Service 호출
```

---

## 2. domain 모듈

**역할**

* 핵심 비즈니스 로직
* JPA Entity 관리
* 트랜잭션 경계

**특징**

* HTTP 개념 없음
* api DTO 사용하지 않음
* 도메인 기준 패키징

### 구조 예시 (Meeting 도메인)

```
domain
 └─ com.xxx.domain.**
    ├─ entity
    │   └─ Moim.java
    ├─ repository
    │   └─ MoimRepository.java
    ├─ service
    │   └─ MoimService.java
    │   └─ MoimServiceImpl.java
    ├─ dto
    │   ├─ command
    │   │   └─ CreateMoimCommand.java
    │   └─ query
    │       └─ MoimSummary.java
    └─ exception
        └─ MoimErrorCode.java
```

### 설계 포인트

* Entity 직접 노출 ❌
* Command / Query DTO 분리
* Service 중심 설계

---

## 3. common 모듈

**역할**

* 공통 인프라 제공
* 모든 모듈에서 재사용

### 포함 대상

* 공통 Exception
* Global Exception Handler
* 공통 Response 포맷
* Util / Config

### 구조 예시

```
common
 └─ com.xxx.common
    ├─ exception
    │   ├─ BusinessException.java
    │   ├─ ErrorCode.java
    │   └─ GlobalExceptionHandler.java
    ├─ response
    │   └─ ApiResponse.java
    ├─ config
    └─ util
```

---

## 예외 처리 전략

* 공통 예외 베이스는 common 에 위치
* 도메인별 ErrorCode 는 domain 에서 정의

```java
throw new BusinessException(MoimErrorCode.Moim_NOT_FOUND);
```

API 계층에서는 GlobalExceptionHandler 를 통해 공통 응답으로 변환합니다.

---

## 이 구조를 선택한 이유

* API 변경이 도메인에 영향을 주지 않도록 분리
* 도메인 로직의 재사용성 확보
* 멀티 실행 모듈 구조로 확장 가능
* 유지보수 및 협업 시 이해하기 쉬운 구조

---

## 요약

* api : HTTP 전용
* domain : 비즈니스 중심
* common : 인프라 공유

이 구조를 통해 프로젝트 규모가 커져도 일관된 설계를 유지할 수 있습니다.
