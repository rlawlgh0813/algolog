package com.algolog.dto.problem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProblemCreateRequest(
    @NotBlank(message = "플랫폼은 필수입니다.")
    @Size(max = 30, message = "플랫폼은 30자 이하여야 합니다.")
    String platform,

    @NotBlank(message = "문제 번호는 필수입니다.")
    @Size(max = 50, message = "문제 번호는 50자 이하여야 합니다.")
    String problemNumber,

    @NotBlank(message = "문제 제목은 필수입니다.")
    @Size(max = 200, message = "문제 제목은 200자 이하여야 합니다.")
    String title,

    @Size(max = 50, message = "난이도는 50자 이하여야 합니다.")
    String difficulty
) {
}
