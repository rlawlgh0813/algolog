package com.algolog.controller.problem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algolog.domain.problem.Problem;
import com.algolog.dto.auth.LoginRequest;
import com.algolog.dto.auth.SignupRequest;
import com.algolog.dto.problem.ProblemCreateRequest;
import com.algolog.repository.problem.ProblemRepository;
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
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemRepository problemRepository;

    @Test
    @DisplayName("문제 등록에 성공하면 문제 정보를 반환한다")
    void createProblem() throws Exception {
        ProblemCreateRequest request = new ProblemCreateRequest(
            uniquePlatform(),
            uniqueProblemNumber(),
            "A+B",
            "Bronze V"
        );

        mockMvc.perform(post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/problems/")))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.platform").value(request.platform()))
            .andExpect(jsonPath("$.problemNumber").value(request.problemNumber()))
            .andExpect(jsonPath("$.title").value("A+B"))
            .andExpect(jsonPath("$.difficulty").value("Bronze V"));
    }

    @Test
    @DisplayName("이미 등록된 플랫폼과 문제 번호 조합이면 409 응답을 반환한다")
    void createDuplicateProblem() throws Exception {
        String platform = uniquePlatform();
        String problemNumber = uniqueProblemNumber();
        ProblemCreateRequest firstRequest = new ProblemCreateRequest(platform, problemNumber, "A+B", "Bronze V");
        ProblemCreateRequest secondRequest = new ProblemCreateRequest(platform, problemNumber, "다른 제목", "Silver V");

        mockMvc.perform(post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken())
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken())
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_PROBLEM"));
    }

    @Test
    @DisplayName("문제 단건 조회에 성공하면 문제 정보를 반환한다")
    void getProblem() throws Exception {
        Problem problem = problemRepository.save(Problem.builder()
            .platform(uniquePlatform())
            .problemNumber(uniqueProblemNumber())
            .title("단건 조회 문제")
            .difficulty("Silver I")
            .build());

        mockMvc.perform(get("/api/problems/{problemId}", problem.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(problem.getId()))
            .andExpect(jsonPath("$.platform").value(problem.getPlatform()))
            .andExpect(jsonPath("$.problemNumber").value(problem.getProblemNumber()))
            .andExpect(jsonPath("$.title").value("단건 조회 문제"))
            .andExpect(jsonPath("$.difficulty").value("Silver I"));
    }

    @Test
    @DisplayName("존재하지 않는 문제를 조회하면 404 응답을 반환한다")
    void getProblemNotFound() throws Exception {
        mockMvc.perform(get("/api/problems/{problemId}", 999_999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("플랫폼, 난이도, 키워드로 문제 목록을 검색한다")
    void searchProblems() throws Exception {
        String platform = uniquePlatform();
        String keyword = "keyword-" + UUID.randomUUID();

        problemRepository.save(Problem.builder()
            .platform(platform)
            .problemNumber("1000-" + keyword)
            .title("검색 대상 문제")
            .difficulty("Gold V")
            .build());

        problemRepository.save(Problem.builder()
            .platform(platform)
            .problemNumber("2000-" + UUID.randomUUID())
            .title("검색 제외 문제")
            .difficulty("Silver V")
            .build());

        mockMvc.perform(get("/api/problems")
                .param("platform", platform)
                .param("difficulty", "Gold V")
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].platform").value(platform))
            .andExpect(jsonPath("$.content[0].difficulty").value("Gold V"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    private String uniquePlatform() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String uniqueProblemNumber() {
        return UUID.randomUUID().toString();
    }

    private String bearerToken() throws Exception {
        String email = "problem-" + UUID.randomUUID() + "@example.com";
        String password = "password1234";

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignupRequest(email, password, "problem-user"))))
            .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String accessToken = objectMapper.readTree(response).get("accessToken").asText();
        return "Bearer " + accessToken;
    }
}
