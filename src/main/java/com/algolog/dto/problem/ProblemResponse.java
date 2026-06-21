package com.algolog.dto.problem;

import com.algolog.domain.problem.Problem;

public record ProblemResponse(
    Long id,
    String platform,
    String problemNumber,
    String title,
    String difficulty
) {

    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
            problem.getId(),
            problem.getPlatform(),
            problem.getProblemNumber(),
            problem.getTitle(),
            problem.getDifficulty()
        );
    }
}
