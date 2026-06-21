package com.algolog.controller.counterexample;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algolog.domain.counterexample.CounterExample;
import com.algolog.domain.problem.Problem;
import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.domain.user.User;
import com.algolog.dto.auth.LoginRequest;
import com.algolog.dto.auth.SignupRequest;
import com.algolog.dto.counterexample.CounterExampleCreateRequest;
import com.algolog.repository.counterexample.CounterExampleRepository;
import com.algolog.repository.problem.ProblemRepository;
import com.algolog.repository.solutionrecord.SolutionRecordRepository;
import com.algolog.repository.user.UserRepository;
import java.util.UUID;
import org.hamcrest.Matchers;
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
class CounterExampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SolutionRecordRepository solutionRecordRepository;

    @Autowired
    private CounterExampleRepository counterExampleRepository;

    @Test
    @DisplayName("풀이 기록 작성자는 반례를 작성할 수 있다")
    void createCounterExample() throws Exception {
        TestUser owner = createUserAndToken("counter-create");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PUBLIC);
        CounterExampleCreateRequest request = new CounterExampleCreateRequest(
            "1 2",
            "3",
            "입력 파싱 실수",
            "공백 기준 split 처리"
        );

        mockMvc.perform(post("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", owner.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location",
                Matchers.startsWith("/api/solution-records/" + solutionRecord.getId() + "/counter-examples/")
            ))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.solutionRecordId").value(solutionRecord.getId()))
            .andExpect(jsonPath("$.inputExample").value("1 2"))
            .andExpect(jsonPath("$.expectedBehavior").value("3"))
            .andExpect(jsonPath("$.wrongReason").value("입력 파싱 실수"))
            .andExpect(jsonPath("$.fixMemo").value("공백 기준 split 처리"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("다른 사용자는 반례를 작성할 수 없다")
    void createCounterExampleByOtherUser() throws Exception {
        TestUser owner = createUserAndToken("counter-owner");
        TestUser other = createUserAndToken("counter-other");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PUBLIC);
        CounterExampleCreateRequest request = new CounterExampleCreateRequest("1 2", "3", null, null);

        mockMvc.perform(post("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", other.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("존재하지 않는 풀이 기록에 반례를 작성하면 404 응답을 반환한다")
    void createCounterExampleWithMissingSolutionRecord() throws Exception {
        TestUser owner = createUserAndToken("counter-missing");
        CounterExampleCreateRequest request = new CounterExampleCreateRequest("1 2", "3", null, null);

        mockMvc.perform(post("/api/solution-records/{solutionRecordId}/counter-examples", 999_999L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", owner.authorizationHeader())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SOLUTION_RECORD_NOT_FOUND"));
    }

    @Test
    @DisplayName("공개 풀이 기록의 반례는 인증 없이 조회할 수 있다")
    void getPublicCounterExamplesWithoutAuthentication() throws Exception {
        TestUser owner = createUserAndToken("counter-public");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PUBLIC);
        createCounterExample(solutionRecord, "1 2");
        createCounterExample(solutionRecord, "3 4");

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].inputExample").value("1 2"))
            .andExpect(jsonPath("$[1].inputExample").value("3 4"));
    }

    @Test
    @DisplayName("비공개 풀이 기록의 반례는 작성자만 조회할 수 있다")
    void getPrivateCounterExamplesByOwner() throws Exception {
        TestUser owner = createUserAndToken("counter-private-owner");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PRIVATE);
        createCounterExample(solutionRecord, "1 2");

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId())
                .header("Authorization", owner.authorizationHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].inputExample").value("1 2"));
    }

    @Test
    @DisplayName("비공개 풀이 기록의 반례는 다른 사용자가 조회할 수 없다")
    void getPrivateCounterExamplesByOtherUser() throws Exception {
        TestUser owner = createUserAndToken("counter-private-owner2");
        TestUser other = createUserAndToken("counter-private-other");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PRIVATE);
        createCounterExample(solutionRecord, "1 2");

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId())
                .header("Authorization", other.authorizationHeader()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("풀이 기록 삭제 시 연결된 반례도 함께 삭제된다")
    void deleteSolutionRecordDeletesCounterExamples() throws Exception {
        TestUser owner = createUserAndToken("counter-delete");
        SolutionRecord solutionRecord = createSolutionRecord(owner.user(), Visibility.PUBLIC);
        createCounterExample(solutionRecord, "1 2");

        mockMvc.perform(delete("/api/solution-records/{solutionRecordId}", solutionRecord.getId())
                .header("Authorization", owner.authorizationHeader()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/solution-records/{solutionRecordId}/counter-examples", solutionRecord.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SOLUTION_RECORD_NOT_FOUND"));
    }

    private CounterExample createCounterExample(SolutionRecord solutionRecord, String inputExample) {
        return counterExampleRepository.save(CounterExample.builder()
            .solutionRecord(solutionRecord)
            .inputExample(inputExample)
            .expectedBehavior("expected")
            .wrongReason("wrong")
            .fixMemo("fix")
            .build());
    }

    private SolutionRecord createSolutionRecord(User author, Visibility visibility) {
        Problem problem = problemRepository.save(Problem.builder()
            .platform("BOJ-" + UUID.randomUUID().toString().substring(0, 8))
            .problemNumber(UUID.randomUUID().toString())
            .title("테스트 문제")
            .difficulty("Bronze V")
            .build());

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
}
