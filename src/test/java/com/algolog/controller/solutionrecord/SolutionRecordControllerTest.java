package com.algolog.controller.solutionrecord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algolog.domain.problem.Problem;
import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.domain.user.User;
import com.algolog.dto.auth.LoginRequest;
import com.algolog.dto.auth.SignupRequest;
import com.algolog.dto.solutionrecord.SolutionRecordCreateRequest;
import com.algolog.dto.solutionrecord.SolutionRecordUpdateRequest;
import com.algolog.repository.problem.ProblemRepository;
import com.algolog.repository.solutionrecord.SolutionRecordRepository;
import com.algolog.repository.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SolutionRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SolutionRecordRepository solutionRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("풀이 기록 작성에 성공하면 생성된 풀이 기록을 반환한다")
    void createSolutionRecord() throws Exception {
        TestUser testUser = createUserAndToken("create");
        Problem problem = createProblem();
        SolutionRecordCreateRequest request = new SolutionRecordCreateRequest(
            problem.getId(),
            "입출력 형식에 주의한 풀이",
            "두 정수를 입력받아 합을 출력한다.",
            "입력 파싱 실수",
            SolvingStatus.SOLVED,
            false,
            Visibility.PUBLIC
        );

        mockMvc.perform(post("/api/solution-records")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testUser.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.problemId").value(problem.getId()))
            .andExpect(jsonPath("$.authorId").value(testUser.user().getId()))
            .andExpect(jsonPath("$.title").value("입출력 형식에 주의한 풀이"))
            .andExpect(jsonPath("$.solvingStatus").value("SOLVED"))
            .andExpect(jsonPath("$.reviewNeeded").value(false))
            .andExpect(jsonPath("$.visibility").value("PUBLIC"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("존재하지 않는 문제로 풀이 기록을 작성하면 404 응답을 반환한다")
    void createSolutionRecordWithMissingProblem() throws Exception {
        TestUser testUser = createUserAndToken("missing-problem");
        SolutionRecordCreateRequest request = new SolutionRecordCreateRequest(
            999_999L,
            "없는 문제 풀이",
            null,
            null,
            SolvingStatus.NOT_SOLVED,
            true,
            Visibility.PRIVATE
        );

        mockMvc.perform(post("/api/solution-records")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testUser.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("내 풀이 기록 목록을 조건으로 조회한다")
    void searchMySolutionRecords() throws Exception {
        TestUser testUser = createUserAndToken("search");
        Problem targetProblem = createProblem(uniquePlatform("BOJ"), "Gold V");
        Problem otherProblem = createProblem(uniquePlatform("PG"), "Silver V");

        solutionRecordRepository.save(SolutionRecord.builder()
            .author(testUser.user())
            .problem(targetProblem)
            .title("검색 대상 풀이")
            .solutionMemo("memo")
            .mistakeNote("mistake")
            .solvingStatus(SolvingStatus.NEED_RETRY)
            .reviewNeeded(true)
            .visibility(Visibility.PRIVATE)
            .build());

        solutionRecordRepository.save(SolutionRecord.builder()
            .author(testUser.user())
            .problem(otherProblem)
            .title("검색 제외 풀이")
            .solvingStatus(SolvingStatus.SOLVED)
            .reviewNeeded(false)
            .visibility(Visibility.PUBLIC)
            .build());

        mockMvc.perform(get("/api/me/solution-records")
                .header("Authorization", testUser.authorizationHeader())
                .param("platform", targetProblem.getPlatform())
                .param("difficulty", "Gold V")
                .param("solvingStatus", "NEED_RETRY")
                .param("reviewNeeded", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].problem.id").value(targetProblem.getId()))
            .andExpect(jsonPath("$.content[0].title").value("검색 대상 풀이"))
            .andExpect(jsonPath("$.content[0].solvingStatus").value("NEED_RETRY"))
            .andExpect(jsonPath("$.content[0].reviewNeeded").value(true))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("풀이 기록 상세 조회에 성공한다")
    void getSolutionRecord() throws Exception {
        TestUser testUser = createUserAndToken("detail");
        SolutionRecord solutionRecord = createSolutionRecord(testUser.user(), createProblem());

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .header("Authorization", testUser.authorizationHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solutionRecord.getId()))
            .andExpect(jsonPath("$.author.id").value(testUser.user().getId()))
            .andExpect(jsonPath("$.problem.id").value(solutionRecord.getProblem().getId()))
            .andExpect(jsonPath("$.title").value(solutionRecord.getTitle()))
            .andExpect(jsonPath("$.counterExamples.length()").value(0));
    }

    @Test
    @DisplayName("공개 풀이 기록은 인증 없이 상세 조회할 수 있다")
    void getPublicSolutionRecordWithoutAuthentication() throws Exception {
        TestUser owner = createUserAndToken("owner");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), createProblem(), Visibility.PUBLIC);

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}", solutionRecord.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solutionRecord.getId()))
            .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    @DisplayName("공개 풀이 기록은 다른 사용자도 상세 조회할 수 있다")
    void getOtherUserPublicSolutionRecord() throws Exception {
        TestUser owner = createUserAndToken("owner-public");
        TestUser other = createUserAndToken("other-public");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), createProblem(), Visibility.PUBLIC);

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .header("Authorization", other.authorizationHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solutionRecord.getId()))
            .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    @DisplayName("비공개 풀이 기록은 인증 없이 조회할 수 없다")
    void getPrivateSolutionRecordWithoutAuthentication() throws Exception {
        TestUser owner = createUserAndToken("owner-private-anonymous");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), createProblem(), Visibility.PRIVATE);

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}", solutionRecord.getId()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("비공개 풀이 기록은 다른 사용자가 조회할 수 없다")
    void getOtherUserPrivateSolutionRecord() throws Exception {
        TestUser owner = createUserAndToken("owner-private");
        TestUser other = createUserAndToken("other");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), createProblem(), Visibility.PRIVATE);

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .header("Authorization", other.authorizationHeader()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("풀이 기록 수정에 성공한다")
    void updateSolutionRecord() throws Exception {
        TestUser testUser = createUserAndToken("update");
        SolutionRecord solutionRecord = createSolutionRecord(testUser.user(), createProblem());
        SolutionRecordUpdateRequest request = new SolutionRecordUpdateRequest(
            "수정된 풀이 메모",
            "수정된 풀이 설명",
            "수정된 실수 포인트",
            SolvingStatus.NEED_RETRY,
            true,
            Visibility.PRIVATE
        );

        mockMvc.perform(patch("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testUser.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solutionRecord.getId()))
            .andExpect(jsonPath("$.title").value("수정된 풀이 메모"))
            .andExpect(jsonPath("$.solvingStatus").value("NEED_RETRY"))
            .andExpect(jsonPath("$.reviewNeeded").value(true))
            .andExpect(jsonPath("$.visibility").value("PRIVATE"))
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("풀이 기록 삭제에 성공한다")
    void deleteSolutionRecord() throws Exception {
        TestUser testUser = createUserAndToken("delete");
        SolutionRecord solutionRecord = createSolutionRecord(testUser.user(), createProblem());

        mockMvc.perform(delete("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .header("Authorization", testUser.authorizationHeader()))
            .andExpect(status().isNoContent());
    }

    private SolutionRecord createSolutionRecord(User author, Problem problem) {
        return createSolutionRecord(author, problem, Visibility.PUBLIC);
    }

    private SolutionRecord createSolutionRecord(User author, Problem problem, Visibility visibility) {
        return solutionRecordRepository.save(SolutionRecord.builder()
            .author(author)
            .problem(problem)
            .title("기본 풀이 기록")
            .solutionMemo("풀이 설명")
            .mistakeNote("실수 메모")
            .solvingStatus(SolvingStatus.SOLVED)
            .reviewNeeded(false)
            .visibility(visibility)
            .build());
    }

    private Problem createProblem() {
        return createProblem(uniquePlatform("BOJ"), "Bronze V");
    }

    private Problem createProblem(String platform, String difficulty) {
        return problemRepository.save(Problem.builder()
            .platform(platform)
            .problemNumber(UUID.randomUUID().toString())
            .title("테스트 문제")
            .difficulty(difficulty)
            .build());
    }

    private TestUser createUserAndToken(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String password = "password1234";

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignupRequest(email, password, prefix))))
            .andExpect(status().isCreated());

        User user = userRepository.findByEmail(email).orElseThrow();
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String accessToken = objectMapper.readTree(response).get("accessToken").asText();
        return new TestUser(user, "Bearer " + accessToken);
    }

    private record TestUser(User user, String authorizationHeader) {
    }

    private String uniquePlatform(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
