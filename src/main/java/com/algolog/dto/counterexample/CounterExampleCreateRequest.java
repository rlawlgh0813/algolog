package com.algolog.dto.counterexample;

import jakarta.validation.constraints.NotBlank;

public record CounterExampleCreateRequest(
    @NotBlank(message = "실패 입력은 필수입니다.")
    String inputExample,

    String expectedBehavior,

    String wrongReason,

    String fixMemo
) {
}
