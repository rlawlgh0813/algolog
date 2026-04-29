# API 명세 초안

이 문서는 AlgoLog MVP 기준 API 초안입니다. 실제 구현 중 DTO 필드명과 응답 형식은 일부 조정될 수 있습니다.

## 1. 기본 정책

### 1.1 Base URL

```text
/api
```

### 1.2 인증 방식

로그인 성공 시 JWT Access Token을 발급합니다.

인증이 필요한 API는 아래 헤더를 사용합니다.

```http
Authorization: Bearer {accessToken}
```

### 1.3 응답 형식

초기 구현에서는 Spring의 기본 `ResponseEntity`와 DTO를 사용합니다.  
프로젝트가 안정화되면 공통 응답 포맷 도입을 고려합니다.

성공 응답 예시:

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "algo_user"
}
```

에러 응답은 전역 예외 처리에서 일관된 형식으로 반환합니다.

```json
{
  "code": "PROBLEM_NOT_FOUND",
  "message": "문제를 찾을 수 없습니다."
}
```

### 1.4 페이징 정책

목록 조회 API는 기본적으로 페이징을 사용합니다.

요청 파라미터:

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| page | 0 | 페이지 번호 |
| size | 20 | 페이지 크기 |
| sort | createdAt,desc | 정렬 기준 |

Spring Data의 `Page` 응답을 그대로 노출하기보다, 추후 별도 페이지 응답 DTO를 사용할 수 있습니다.

## 2. 인증 API

### 2.1 회원가입

```http
POST /api/auth/signup
```

인증: 불필요

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "algo_user"
}
```

Response:

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "algo_user"
}
```

Validation:

| 필드 | 조건 |
| --- | --- |
| email | 필수, 이메일 형식, 중복 불가 |
| password | 필수, 최소 길이 정책 적용 예정 |
| nickname | 필수 |

예외:

- `DUPLICATE_EMAIL`
- `VALIDATION_ERROR`

### 2.2 로그인

```http
POST /api/auth/login
```

인증: 불필요

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Response:

```json
{
  "accessToken": "jwt.access.token",
  "tokenType": "Bearer"
}
```

예외:

- `INVALID_LOGIN`
- `VALIDATION_ERROR`

## 3. 문제 API

### 3.1 문제 등록

```http
POST /api/problems
```

인증: 필요

Request:

```json
{
  "platform": "BOJ",
  "problemNumber": "1000",
  "title": "A+B",
  "difficulty": "Bronze V"
}
```

Response:

```json
{
  "id": 1,
  "platform": "BOJ",
  "problemNumber": "1000",
  "title": "A+B",
  "difficulty": "Bronze V"
}
```

정책:

- `platform + problemNumber` 조합은 중복 등록을 막습니다.
- 문제는 특정 사용자 소유가 아니라 서비스 전체에서 공유되는 메타데이터로 봅니다.

예외:

- `DUPLICATE_PROBLEM`
- `VALIDATION_ERROR`

### 3.2 문제 목록 조회

```http
GET /api/problems
```

인증: 불필요

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| platform | 아니오 | 플랫폼 필터 |
| difficulty | 아니오 | 난이도 필터 |
| keyword | 아니오 | 문제 번호 또는 제목 검색 |
| page | 아니오 | 페이지 번호 |
| size | 아니오 | 페이지 크기 |

Response:

```json
{
  "content": [
    {
      "id": 1,
      "platform": "BOJ",
      "problemNumber": "1000",
      "title": "A+B",
      "difficulty": "Bronze V"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 3.3 문제 단건 조회

```http
GET /api/problems/{problemId}
```

인증: 불필요

Response:

```json
{
  "id": 1,
  "platform": "BOJ",
  "problemNumber": "1000",
  "title": "A+B",
  "difficulty": "Bronze V"
}
```

예외:

- `PROBLEM_NOT_FOUND`

## 4. 풀이 기록 API

### 4.1 풀이 기록 작성

```http
POST /api/solution-records
```

인증: 필요

Request:

```json
{
  "problemId": 1,
  "title": "입출력 형식에 주의한 풀이",
  "solutionMemo": "두 정수를 입력받아 합을 출력한다.",
  "mistakeNote": "처음에 입력 파싱을 잘못 처리했다.",
  "solvingStatus": "SOLVED",
  "reviewNeeded": false,
  "visibility": "PUBLIC"
}
```

Response:

```json
{
  "id": 1,
  "problemId": 1,
  "authorId": 1,
  "title": "입출력 형식에 주의한 풀이",
  "solvingStatus": "SOLVED",
  "reviewNeeded": false,
  "visibility": "PUBLIC",
  "createdAt": "2026-04-29T10:00:00"
}
```

예외:

- `PROBLEM_NOT_FOUND`
- `VALIDATION_ERROR`

### 4.2 내 풀이 기록 목록 조회

```http
GET /api/me/solution-records
```

인증: 필요

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| platform | 아니오 | 문제 플랫폼 필터 |
| difficulty | 아니오 | 문제 난이도 필터 |
| solvingStatus | 아니오 | 해결 상태 필터 |
| reviewNeeded | 아니오 | 복습 필요 여부 |
| page | 아니오 | 페이지 번호 |
| size | 아니오 | 페이지 크기 |

Response:

```json
{
  "content": [
    {
      "id": 1,
      "problem": {
        "id": 1,
        "platform": "BOJ",
        "problemNumber": "1000",
        "title": "A+B",
        "difficulty": "Bronze V"
      },
      "title": "입출력 형식에 주의한 풀이",
      "solvingStatus": "SOLVED",
      "reviewNeeded": false,
      "visibility": "PUBLIC",
      "createdAt": "2026-04-29T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 4.3 풀이 기록 상세 조회

```http
GET /api/solution-records/{solutionRecordId}
```

인증:

- 공개 풀이: 불필요
- 비공개 풀이: 작성자 인증 필요

Response:

```json
{
  "id": 1,
  "author": {
    "id": 1,
    "nickname": "algo_user"
  },
  "problem": {
    "id": 1,
    "platform": "BOJ",
    "problemNumber": "1000",
    "title": "A+B",
    "difficulty": "Bronze V"
  },
  "title": "입출력 형식에 주의한 풀이",
  "solutionMemo": "두 정수를 입력받아 합을 출력한다.",
  "mistakeNote": "처음에 입력 파싱을 잘못 처리했다.",
  "solvingStatus": "SOLVED",
  "reviewNeeded": false,
  "visibility": "PUBLIC",
  "counterExamples": [
    {
      "id": 1,
      "inputExample": "1 2",
      "expectedBehavior": "3",
      "wrongReason": "개행 처리 실수",
      "fixMemo": "Scanner 대신 BufferedReader 사용"
    }
  ],
  "createdAt": "2026-04-29T10:00:00",
  "updatedAt": "2026-04-29T10:00:00"
}
```

예외:

- `SOLUTION_RECORD_NOT_FOUND`
- `ACCESS_DENIED`

### 4.4 풀이 기록 수정

```http
PATCH /api/solution-records/{solutionRecordId}
```

인증: 작성자만 가능

Request:

```json
{
  "title": "수정된 풀이 메모",
  "solutionMemo": "수정된 풀이 설명",
  "mistakeNote": "수정된 실수 포인트",
  "solvingStatus": "NEED_RETRY",
  "reviewNeeded": true,
  "visibility": "PRIVATE"
}
```

Response:

```json
{
  "id": 1,
  "title": "수정된 풀이 메모",
  "solvingStatus": "NEED_RETRY",
  "reviewNeeded": true,
  "visibility": "PRIVATE",
  "updatedAt": "2026-04-29T11:00:00"
}
```

예외:

- `SOLUTION_RECORD_NOT_FOUND`
- `ACCESS_DENIED`
- `VALIDATION_ERROR`

### 4.5 풀이 기록 삭제

```http
DELETE /api/solution-records/{solutionRecordId}
```

인증: 작성자만 가능

Response:

```http
204 No Content
```

정책:

- MVP에서는 물리 삭제를 사용합니다.
- 연결된 CounterExample도 함께 삭제합니다.

예외:

- `SOLUTION_RECORD_NOT_FOUND`
- `ACCESS_DENIED`

## 5. 반례 API

### 5.1 반례 작성

```http
POST /api/solution-records/{solutionRecordId}/counter-examples
```

인증: 풀이 기록 작성자만 가능

Request:

```json
{
  "inputExample": "1 2",
  "expectedBehavior": "3",
  "wrongReason": "입력 파싱을 잘못 처리했다.",
  "fixMemo": "공백 기준 split 처리로 수정했다."
}
```

Response:

```json
{
  "id": 1,
  "solutionRecordId": 1,
  "inputExample": "1 2",
  "expectedBehavior": "3",
  "wrongReason": "입력 파싱을 잘못 처리했다.",
  "fixMemo": "공백 기준 split 처리로 수정했다.",
  "createdAt": "2026-04-29T10:10:00"
}
```

예외:

- `SOLUTION_RECORD_NOT_FOUND`
- `ACCESS_DENIED`
- `VALIDATION_ERROR`

### 5.2 특정 풀이 기록의 반례 목록 조회

```http
GET /api/solution-records/{solutionRecordId}/counter-examples
```

인증:

- 공개 풀이의 반례: 불필요
- 비공개 풀이의 반례: 작성자 인증 필요

Response:

```json
[
  {
    "id": 1,
    "inputExample": "1 2",
    "expectedBehavior": "3",
    "wrongReason": "입력 파싱을 잘못 처리했다.",
    "fixMemo": "공백 기준 split 처리로 수정했다.",
    "createdAt": "2026-04-29T10:10:00"
  }
]
```

예외:

- `SOLUTION_RECORD_NOT_FOUND`
- `ACCESS_DENIED`

## 6. 공개 풀이 탐색 API

### 6.1 공개 풀이 목록 조회

```http
GET /api/public/solution-records
```

인증: 불필요

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| platform | 아니오 | 문제 플랫폼 필터 |
| difficulty | 아니오 | 문제 난이도 필터 |
| solvingStatus | 아니오 | 해결 상태 필터 |
| reviewNeeded | 아니오 | 복습 필요 여부 필터 |
| page | 아니오 | 페이지 번호 |
| size | 아니오 | 페이지 크기 |

정책:

- `visibility = PUBLIC`인 풀이 기록만 반환합니다.

### 6.2 특정 문제의 공개 풀이 조회

```http
GET /api/problems/{problemId}/public-solution-records
```

인증: 불필요

정책:

- 특정 Problem에 연결된 공개 풀이만 반환합니다.

예외:

- `PROBLEM_NOT_FOUND`

## 7. Enum 값

### 7.1 SolvingStatus

| 값 | 설명 |
| --- | --- |
| NOT_SOLVED | 아직 해결하지 못함 |
| SOLVED | 해결 완료 |
| NEED_RETRY | 다시 풀어볼 필요 있음 |

### 7.2 Visibility

| 값 | 설명 |
| --- | --- |
| PUBLIC | 공개 |
| PRIVATE | 비공개 |

## 8. 주요 예외 코드

| 코드 | HTTP Status | 설명 |
| --- | --- | --- |
| VALIDATION_ERROR | 400 | 요청값 검증 실패 |
| DUPLICATE_EMAIL | 409 | 이미 사용 중인 이메일 |
| DUPLICATE_PROBLEM | 409 | 이미 등록된 문제 |
| INVALID_LOGIN | 401 | 이메일 또는 비밀번호 불일치 |
| UNAUTHORIZED | 401 | 인증 필요 |
| ACCESS_DENIED | 403 | 접근 권한 없음 |
| USER_NOT_FOUND | 404 | 사용자 없음 |
| PROBLEM_NOT_FOUND | 404 | 문제 없음 |
| SOLUTION_RECORD_NOT_FOUND | 404 | 풀이 기록 없음 |
| COUNTER_EXAMPLE_NOT_FOUND | 404 | 반례 없음 |
| INTERNAL_SERVER_ERROR | 500 | 서버 내부 오류 |

## 9. 구현 시 주의할 점

- Entity를 API 응답으로 직접 반환하지 않고 DTO를 사용합니다.
- 권한 검증은 Controller가 아니라 Service 계층에서 처리합니다.
- 비공개 풀이 조회와 반례 조회는 같은 권한 정책을 공유합니다.
- 목록 조회는 처음부터 페이징을 적용합니다.
- Problem 중복 등록은 Service 검증과 DB 유니크 제약을 함께 고려합니다.
