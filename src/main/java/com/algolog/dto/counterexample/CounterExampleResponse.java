package com.algolog.dto.counterexample;

import com.algolog.domain.counterexample.CounterExample;
import java.time.LocalDateTime;

public record CounterExampleResponse(
    Long id,
    Long solutionRecordId,
    String inputExample,
    String expectedBehavior,
    String wrongReason,
    String fixMemo,
    LocalDateTime createdAt
) {

    public static CounterExampleResponse from(CounterExample counterExample) {
        return new CounterExampleResponse(
            counterExample.getId(),
            counterExample.getSolutionRecord().getId(),
            counterExample.getInputExample(),
            counterExample.getExpectedBehavior(),
            counterExample.getWrongReason(),
            counterExample.getFixMemo(),
            counterExample.getCreatedAt()
        );
    }
}
