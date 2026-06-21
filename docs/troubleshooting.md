# Troubleshooting

이 문서는 AlgoLog를 구현하면서 마주친 문제와 해결 과정을 기록하기 위한 문서입니다.

모든 에러를 기록하지는 않습니다. 포트폴리오와 면접에서 설명할 가치가 있는 문제, 즉 **원인 분석과 설계 판단이 들어간 문제**를 중심으로 남깁니다.

## 기록 기준

아래 조건 중 하나 이상에 해당하면 기록합니다.

- 원인 파악에 시간이 걸린 문제
- JPA, Spring Security, JWT, Validation 등 백엔드 핵심 기술과 관련된 문제
- 여러 해결 방법 중 하나를 선택해야 했던 문제
- 도메인 설계나 권한 정책에 영향을 준 문제
- 재발 가능성이 있어 나중에 다시 참고할 가치가 있는 문제

## 기록하지 않아도 되는 것

- 단순 오타
- import 누락
- IDE 자동 수정으로 바로 해결되는 문제
- 검색 한 번으로 바로 해결한 설정 실수

## 작성 템플릿

```md
## 문제 제목

### 상황

어떤 기능을 구현하던 중 어떤 문제가 발생했는지 적는다.

### 원인

처음 추정한 원인과 실제 원인을 구분해서 적는다.

### 해결

어떤 방식으로 해결했는지 적는다.

### 선택 이유

다른 해결 방법이 있었다면 무엇이 있었고, 왜 현재 방식을 선택했는지 적는다.

### 배운 점

다음에 비슷한 문제를 만나면 어떻게 판단할지 적는다.
```

## 기록 후보

아래 항목은 구현 중 실제로 문제가 발생하거나 설계 판단이 필요할 때 기록합니다.

### 1. 비공개 풀이 기록 접근 권한 검증 위치

예상 쟁점:

- Controller에서 검증할지 Service에서 검증할지
- 공개 글은 누구나 조회 가능하지만, 비공개 글은 작성자만 조회 가능하도록 어디에서 보장할지

기록 가치:

- 인증과 인가의 차이를 설명하기 좋습니다.
- 권한 검증을 비즈니스 규칙으로 볼지, HTTP 계층의 책임으로 볼지 판단하는 근거를 남길 수 있습니다.

### 2. Entity를 API 응답으로 직접 반환하지 않는 이유

예상 쟁점:

- Entity를 그대로 반환하면 빠르게 구현할 수 있지만, 내부 구조가 API에 노출될 수 있습니다.
- JPA 연관관계가 JSON 직렬화 과정에서 순환 참조나 불필요한 로딩을 만들 수 있습니다.

기록 가치:

- DTO를 사용하는 이유를 면접에서 설명하기 좋습니다.

### 3. Problem 중복 등록 제약 위치

예상 쟁점:

- `platform + problemNumber` 중복을 DB 유니크 제약으로 막을지, Service에서 먼저 검사할지
- 동시 요청이 들어왔을 때 Service 검사만으로 충분한지

기록 가치:

- 애플리케이션 검증과 DB 제약의 역할 차이를 설명하기 좋습니다.

### 4. JPA 연관관계와 조회 성능

예상 쟁점:

- SolutionRecord 목록 조회 시 Problem, User 정보를 함께 보여줄 때 N+1 문제가 생길 수 있습니다.
- fetch join, EntityGraph, DTO projection 중 어떤 방식을 선택할지 고민할 수 있습니다.

기록 가치:

- JPA를 단순 CRUD 이상으로 이해하고 있음을 보여줄 수 있습니다.

### 5. JWT 인증 실패 응답 처리

예상 쟁점:

- 인증 실패가 ControllerAdvice로 처리되지 않는 이유
- Security Filter 단계에서 발생한 예외를 어떻게 JSON 응답으로 내려줄지

기록 가치:

- Spring MVC 예외 처리와 Spring Security 필터 체인의 차이를 설명하기 좋습니다.

## 작성 예시

## 비공개 풀이 기록 접근 권한 검증 위치

### 상황

풀이 기록 상세 조회 기능에서 공개 글은 누구나 조회할 수 있고, 비공개 글은 작성자만 조회할 수 있어야 했다.

### 원인

처음에는 Controller에서 로그인 사용자와 작성자를 비교하려 했지만, 같은 권한 검증이 상세 조회, 수정, 삭제, 반례 작성 등 여러 기능에 반복될 가능성이 있었다.

### 해결

SolutionRecordService에서 조회 대상 SolutionRecord와 현재 사용자 정보를 함께 받아 권한을 검증하도록 설계했다.

### 선택 이유

Controller는 HTTP 요청과 응답 처리에 집중하고, "비공개 글은 작성자만 볼 수 있다"는 도메인 규칙은 Service 계층에 두는 것이 책임 분리가 명확하다고 판단했다.

### 배운 점

권한 검증은 단순한 요청 처리 로직이 아니라 서비스 정책에 가까우므로, 재사용성과 일관성을 고려해 Service 계층에서 처리하는 편이 적절하다.

## Spring Security 인증 실패 응답 처리

### 상황

JWT 인증 필터를 추가하면서 인증이 필요한 API에 토큰 없이 접근하면 JSON 에러 응답을 내려야 했다.

### 원인

Controller나 Service에서 발생한 `BusinessException`은 `GlobalExceptionHandler`가 처리하지만, 인증 실패는 Spring MVC Controller에 도달하기 전 Security Filter Chain에서 발생한다. 따라서 `@RestControllerAdvice`만으로는 `UNAUTHORIZED`, `ACCESS_DENIED` 응답 형식을 통일할 수 없었다.

### 해결

`SecurityConfig`의 `exceptionHandling`에 `authenticationEntryPoint`와 `accessDeniedHandler`를 설정하고, `SecurityErrorResponseWriter`가 프로젝트의 `ErrorResponse` 형식과 같은 JSON을 직접 쓰도록 했다.

### 선택 이유

인증/인가 실패는 Security 계층의 책임이므로, ControllerAdvice로 억지로 끌어오기보다 Security 설정에서 응답 형식을 맞추는 편이 책임 경계가 명확하다.

### 배운 점

Spring Security 필터 단계의 예외와 Spring MVC 계층의 예외는 처리 경로가 다르다. 인증/인가 실패 응답을 통일하려면 Security 전용 진입점을 설정해야 한다.

## 공개/비공개 풀이 조회 권한 검증 위치

### 상황

풀이 기록 상세 조회와 반례 목록 조회에서 공개 풀이 기록은 누구나 볼 수 있고, 비공개 풀이 기록은 작성자만 볼 수 있어야 했다.

### 원인

Security 설정만으로는 `SolutionRecord.visibility`와 작성자 ID를 함께 고려하기 어렵다. URL 패턴만으로 공개/비공개 여부를 알 수 없기 때문이다.

### 해결

GET 엔드포인트는 Security 레벨에서 열어두고, Service 계층에서 조회한 `SolutionRecord`의 `visibility`와 현재 사용자 ID를 비교해 접근 가능 여부를 판단했다.

### 선택 이유

공개/비공개는 HTTP 경로 규칙이 아니라 도메인 데이터에 기반한 정책이다. 따라서 Service 계층에서 처리하면 상세 조회와 반례 조회가 같은 정책을 재사용할 수 있다.

### 배운 점

권한 검증은 모두 Security 설정에 넣는 것이 아니라, URL만으로 판단 가능한 인증 요구와 도메인 데이터가 필요한 인가 규칙을 나누는 것이 좋다.

## 풀이 기록 삭제 시 반례 삭제 처리

### 상황

`SolutionRecord`를 삭제할 때 연결된 `CounterExample`도 함께 삭제되어야 했다. 테스트에서 반례가 연결된 풀이 기록 삭제가 실패했다.

### 원인

현재 엔티티 관계는 `CounterExample -> SolutionRecord` 단방향 `ManyToOne`만 열려 있다. `SolutionRecord` 쪽에 컬렉션과 cascade를 두지 않았기 때문에, 부모인 풀이 기록을 먼저 삭제하면 DB 외래 키 제약에 걸릴 수 있다.

### 해결

`SolutionRecordService.delete`에서 `CounterExampleRepository.deleteAllBySolutionRecordId`를 먼저 호출한 뒤 `SolutionRecord`를 삭제하도록 명시했다.

### 선택 이유

초기 설계 원칙이 양방향 연관관계를 최소화하는 것이었으므로, 삭제 하나를 위해 `SolutionRecord`에 컬렉션을 추가하기보다 Repository에서 명시 삭제하는 방식이 더 단순하고 의도가 분명했다.

### 배운 점

JPA cascade는 편리하지만 연관관계 방향과 객체 그래프를 넓힌다. 단방향 관계를 유지하는 설계에서는 삭제 순서를 Service 계층에서 명시하는 방식도 충분히 실용적이다.
