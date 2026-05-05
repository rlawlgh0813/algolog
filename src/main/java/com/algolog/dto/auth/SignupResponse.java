package com.algolog.dto.auth;

import com.algolog.domain.user.User;

public record SignupResponse(
    Long id,
    String email,
    String nickname
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}