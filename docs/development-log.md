# Development Log

AlgoLog 구현 과정을 이슈 단위로 기록합니다. GitHub Issue를 함께 사용할 수 있으면 같은 번호로 연결합니다.

## #1 Spring Boot 프로젝트 초기 세팅

### 브랜치

- `feature/#1-spring-boot-setup`

### 목적

문서 중심으로 정리되어 있던 AlgoLog 레포를 실제 Spring Boot 백엔드 프로젝트로 전환합니다.

### 작업 내용

- Spring Boot Gradle 프로젝트 생성
- Gradle Wrapper 추가
- 기본 애플리케이션 클래스 생성
- 테스트 기본 클래스 생성
- H2 기반 로컬 개발 DB 설정
- JPA 기본 설정 추가
- README 진행 상태와 로컬 실행 방법 갱신

### 포함 의존성

- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Lombok
- H2 Database
- MySQL Driver

### 검증

```powershell
.\gradlew.bat test
```

결과: 성공

### 메모

현재 로컬 환경에 GitHub CLI(`gh`)가 설치되어 있지 않아 GitHub Issue 자동 생성은 보류했습니다. 이후 `#1` 이슈를 GitHub에서 생성하면 이 브랜치와 커밋을 연결할 수 있습니다.
## #3 도메인 엔티티 구현

### 브랜치

- `feature/#3-domain-entities`

### 목적

문서에 정리한 AlgoLog 핵심 도메인을 JPA 엔티티로 구현합니다.

### 작업 내용

- `BaseTimeEntity` 추가
- `User` 엔티티 추가
- `Problem` 엔티티 추가
- `SolutionRecord` 엔티티 추가
- `CounterExample` 엔티티 추가
- `SolvingStatus`, `Visibility` enum 추가
- JPA Auditing 활성화

### 설계 메모

- `User` 테이블은 예약어 충돌을 피하기 위해 `users`로 지정했습니다.
- `Problem`은 `platform + problemNumber` 조합에 유니크 제약을 둡니다.
- 초기 구현에서는 양방향 연관관계를 열지 않고, `SolutionRecord`, `CounterExample`에서 필요한 `ManyToOne`만 둡니다.
- `createdAt`, `updatedAt`은 `BaseTimeEntity`와 JPA Auditing으로 공통 처리합니다.

### 트러블슈팅

PowerShell `Set-Content -Encoding UTF8`로 Java 파일을 생성하면서 BOM 문자가 붙어 `illegal character: '\ufeff'` 컴파일 오류가 발생했습니다. Java 파일을 UTF-8 BOM 없이 다시 저장해 해결했습니다.

### 검증

```powershell
.\gradlew.bat test
```

결과: 성공

## #9 로그인 API와 JWT 발급 구현

### 브랜치

- `feature/#9-auth-login-jwt`

### 목적

회원가입한 사용자가 이메일과 비밀번호로 로그인하고, 이후 인증 API에서 사용할 JWT Access Token을 발급받을 수 있게 합니다.

### 작업 내용

- `LoginRequest`, `LoginResponse` DTO 추가
- 로그인 API 추가
- 이메일 기반 사용자 조회와 BCrypt 비밀번호 검증 추가
- HMAC-SHA256 기반 JWT Access Token 발급 컴포넌트 추가
- JWT 설정값 추가
  - `JWT_SECRET`
  - `JWT_ACCESS_TOKEN_EXPIRATION_MILLIS`
- Security 설정에 로그인 엔드포인트 허용과 stateless 세션 정책 추가
- 로그인 성공/실패 API 테스트 추가

### 설계 메모

- 로그인 실패는 계정 존재 여부를 노출하지 않도록 `INVALID_LOGIN`으로 통일했습니다.
- JWT secret과 만료 시간은 환경변수로 덮어쓸 수 있게 하고, 로컬 개발 기본값만 `application.yml`에 두었습니다.
- 이번 단계에서는 토큰 발급까지만 구현하고, 요청마다 토큰을 검증하는 인증 필터와 권한 실패 응답은 이후 인증/인가 이슈에서 보강합니다.

### 검증

```bash
./gradlew test
```

결과: 성공
## #5 전역 예외 처리 구조 구현

### 브랜치

- `feature/#5-global-exception`

### 목적

기능별 API 구현 전에 예외 응답 형식을 통일하여 인증, Problem, SolutionRecord 기능에서 같은 방식으로 실패 응답을 내려줄 수 있게 합니다.

### 작업 내용

- `ErrorCode` enum 추가
- `BusinessException` 추가
- `ErrorResponse` 추가
- `GlobalExceptionHandler` 추가
- Bean Validation 실패 응답 처리

### 설계 메모

- `ErrorCode`는 HTTP 상태와 기본 메시지를 함께 가집니다.
- Service 계층에서는 `BusinessException`을 던지고, ControllerAdvice가 HTTP 응답으로 변환합니다.
- Validation 실패는 필드별 오류를 `fieldErrors`에 담아 반환합니다.
- Spring Security Filter 단계에서 발생하는 인증/인가 예외는 이후 인증 구현 단계에서 별도 entry point/access denied handler로 보강합니다.

### 검증

```powershell
.\gradlew.bat test
```

결과: 성공
## #7 회원가입 API 구현

### 브랜치

- `feature/#7-auth-signup`

### 목적

사용자가 이메일, 비밀번호, 닉네임으로 가입할 수 있는 API를 구현합니다.

### 작업 내용

- `UserRepository` 추가
- `SignupRequest`, `SignupResponse` DTO 추가
- `AuthService` 추가
- `AuthController` 추가
- `SecurityConfig` 추가
- BCrypt 기반 `PasswordEncoder` 설정
- 이메일 중복 검증 추가
- 회원가입 성공/중복 이메일 API 테스트 추가

### 설계 메모

- 비밀번호는 평문으로 저장하지 않고 BCrypt로 암호화합니다.
- Entity를 API 응답으로 직접 반환하지 않고 응답 DTO를 사용합니다.
- 이메일 중복은 Service 계층에서 먼저 검증하고, 이후 DB 유니크 제약과 함께 보호합니다.
- JWT 구현 전까지는 Security 설정에서 요청을 임시로 허용하고, 로그인/JWT 단계에서 인증 필요한 API를 잠글 예정입니다.

### 트러블슈팅

Spring Boot 4에서는 기존 Boot 3 예제와 달리 `AutoConfigureMockMvc` 패키지가 `org.springframework.boot.webmvc.test.autoconfigure`로 이동했고, Jackson도 3.x 계열의 `tools.jackson.databind.ObjectMapper`를 사용합니다. 테스트 컴파일 실패 후 Gradle 의존성과 jar 내부 패키지를 확인해 수정했습니다.

### 검증

```powershell
.\gradlew.bat test
```

결과: 성공
