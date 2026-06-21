package com.algolog.dto.solutionrecord;

import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import java.time.LocalDateTime;

public record SolutionRecordResponse(
    Long id,
    Long problemId,
    Long authorId,
    String title,
    SolvingStatus solvingStatus,
    boolean reviewNeeded,
    Visibility visibility,
    LocalDateTime createdAt
) {

    public static SolutionRecordResponse from(SolutionRecord solutionRecord) {
        return new SolutionRecordResponse(
            solutionRecord.getId(),
            solutionRecord.getProblem().getId(),
            solutionRecord.getAuthor().getId(),
            solutionRecord.getTitle(),
            solutionRecord.getSolvingStatus(),
            solutionRecord.isReviewNeeded(),
            solutionRecord.getVisibility(),
            solutionRecord.getCreatedAt()
        );
    }
}
