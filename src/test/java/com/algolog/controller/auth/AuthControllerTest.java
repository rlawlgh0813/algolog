package com.algolog.controller.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.matchesPattern;

import com.algolog.dto.auth.LoginRequest;
import com.algolog.dto.auth.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입에 성공하면 사용자 정보를 반환한다")
    void signup() throws Exception {
        SignupRequest request = new SignupRequest("new-user@example.com", "password1234", "newbie");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("new-user@example.com"))
            .andExpect(jsonPath("$.nickname").value("newbie"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 409 응답을 반환한다")
    void signupWithDuplicateEmail() throws Exception {
        SignupRequest firstRequest = new SignupRequest("duplicate@example.com", "password1234", "first");
        SignupRequest secondRequest = new SignupRequest("duplicate@example.com", "password5678", "second");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("로그인에 성공하면 JWT access token을 반환한다")
    void login() throws Exception {
        SignupRequest signupRequest = new SignupRequest("login-user@example.com", "password1234", "loginUser");
        LoginRequest loginRequest = new LoginRequest("login-user@example.com", "password1234");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("로그인 비밀번호가 틀리면 401 응답을 반환한다")
    void loginWithInvalidPassword() throws Exception {
        SignupRequest signupRequest = new SignupRequest("invalid-login@example.com", "password1234", "invalidLogin");
        LoginRequest loginRequest = new LoginRequest("invalid-login@example.com", "wrong-password");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_LOGIN"));
    }
}
