package com.algolog.dto.auth;

public record LoginResponse(
    String accessToken,
    String tokenType
) {

    private static final String BEARER_TYPE = "Bearer";

    public static LoginResponse bearer(String accessToken) {
        return new LoginResponse(accessToken, BEARER_TYPE);
    }
}
