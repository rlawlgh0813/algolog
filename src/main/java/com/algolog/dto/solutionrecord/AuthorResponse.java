package com.algolog.dto.solutionrecord;

import com.algolog.domain.user.User;

public record AuthorResponse(
    Long id,
    String nickname
) {

    public static AuthorResponse from(User user) {
        return new AuthorResponse(user.getId(), user.getNickname());
    }
}
