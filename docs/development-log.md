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
