package com.algolog.dto.solutionrecord;

import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.dto.problem.ProblemResponse;
import java.time.LocalDateTime;

public record MySolutionRecordResponse(
    Long id,
    ProblemResponse problem,
    String title,
    SolvingStatus solvingStatus,
    boolean reviewNeeded,
    Visibility visibility,
    LocalDateTime createdAt
) {

    public static MySolutionRecordResponse from(SolutionRecord solutionRecord) {
        return new MySolutionRecordResponse(
            solutionRecord.getId(),
            ProblemResponse.from(solutionRecord.getProblem()),
            solutionRecord.getTitle(),
            solutionRecord.getSolvingStatus(),
            solutionRecord.isReviewNeeded(),
            solutionRecord.getVisibility(),
            solutionRecord.getCreatedAt()
        );
    }
}
