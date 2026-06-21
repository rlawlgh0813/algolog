package com.algolog.dto.solutionrecord;

import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.dto.problem.ProblemResponse;
import java.time.LocalDateTime;
import java.util.List;

public record SolutionRecordDetailResponse(
    Long id,
    AuthorResponse author,
    ProblemResponse problem,
    String title,
    String solutionMemo,
    String mistakeNote,
    SolvingStatus solvingStatus,
    boolean reviewNeeded,
    Visibility visibility,
    List<Object> counterExamples,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static SolutionRecordDetailResponse from(SolutionRecord solutionRecord) {
        return new SolutionRecordDetailResponse(
            solutionRecord.getId(),
            AuthorResponse.from(solutionRecord.getAuthor()),
            ProblemResponse.from(solutionRecord.getProblem()),
            solutionRecord.getTitle(),
            solutionRecord.getSolutionMemo(),
            solutionRecord.getMistakeNote(),
            solutionRecord.getSolvingStatus(),
            solutionRecord.isReviewNeeded(),
            solutionRecord.getVisibility(),
            List.of(),
            solutionRecord.getCreatedAt(),
            solutionRecord.getUpdatedAt()
        );
    }
}
