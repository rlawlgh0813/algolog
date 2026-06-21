package com.algolog.dto.solutionrecord;

import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolutionRecordCreateRequest(
    @NotNull(message = "문제 ID는 필수입니다.")
    Long problemId,

    @NotBlank(message = "풀이 기록 제목은 필수입니다.")
    @Size(max = 200, message = "풀이 기록 제목은 200자 이하여야 합니다.")
    String title,

    String solutionMemo,

    String mistakeNote,

    @NotNull(message = "해결 상태는 필수입니다.")
    SolvingStatus solvingStatus,

    boolean reviewNeeded,

    @NotNull(message = "공개 여부는 필수입니다.")
    Visibility visibility
) {
}
