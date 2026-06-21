package com.algolog.dto.solutionrecord;

import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import java.time.LocalDateTime;

public record SolutionRecordUpdateResponse(
    Long id,
    String title,
    SolvingStatus solvingStatus,
    boolean reviewNeeded,
    Visibility visibility,
    LocalDateTime updatedAt
) {

    public static SolutionRecordUpdateResponse from(SolutionRecord solutionRecord) {
        return new SolutionRecordUpdateResponse(
            solutionRecord.getId(),
            solutionRecord.getTitle(),
            solutionRecord.getSolvingStatus(),
            solutionRecord.isReviewNeeded(),
            solutionRecord.getVisibility(),
            solutionRecord.getUpdatedAt()
        );
    }
}
