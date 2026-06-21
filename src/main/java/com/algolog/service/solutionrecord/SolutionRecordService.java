package com.algolog.service.solutionrecord;

import com.algolog.domain.problem.Problem;
import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.domain.user.User;
import com.algolog.dto.common.PageResponse;
import com.algolog.dto.solutionrecord.MySolutionRecordResponse;
import com.algolog.dto.solutionrecord.SolutionRecordCreateRequest;
import com.algolog.dto.solutionrecord.SolutionRecordDetailResponse;
import com.algolog.dto.solutionrecord.SolutionRecordResponse;
import com.algolog.dto.solutionrecord.SolutionRecordUpdateRequest;
import com.algolog.dto.solutionrecord.SolutionRecordUpdateResponse;
import com.algolog.global.exception.BusinessException;
import com.algolog.global.exception.ErrorCode;
import com.algolog.repository.problem.ProblemRepository;
import com.algolog.repository.solutionrecord.SolutionRecordRepository;
import com.algolog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolutionRecordService {

    private final SolutionRecordRepository solutionRecordRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    @Transactional
    public SolutionRecordResponse create(Long authorId, SolutionRecordCreateRequest request) {
        User author = getUser(authorId);
        Problem problem = problemRepository.findById(request.problemId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        SolutionRecord solutionRecord = SolutionRecord.builder()
            .author(author)
            .problem(problem)
            .title(request.title())
            .solutionMemo(request.solutionMemo())
            .mistakeNote(request.mistakeNote())
            .solvingStatus(request.solvingStatus())
            .reviewNeeded(request.reviewNeeded())
            .visibility(request.visibility())
            .build();

        return SolutionRecordResponse.from(solutionRecordRepository.save(solutionRecord));
    }

    @Transactional(readOnly = true)
    public PageResponse<MySolutionRecordResponse> searchMine(
        Long authorId,
        String platform,
        String difficulty,
        SolvingStatus solvingStatus,
        Boolean reviewNeeded,
        Pageable pageable
    ) {
        getUser(authorId);
        return PageResponse.from(solutionRecordRepository.searchMine(
            authorId,
            normalize(platform),
            normalize(difficulty),
            solvingStatus,
            reviewNeeded,
            pageable
        ).map(MySolutionRecordResponse::from));
    }

    @Transactional(readOnly = true)
    public SolutionRecordDetailResponse getById(Long currentUserId, Long solutionRecordId) {
        SolutionRecord solutionRecord = getSolutionRecord(solutionRecordId);
        validateReadable(currentUserId, solutionRecord);
        return SolutionRecordDetailResponse.from(solutionRecord);
    }

    @Transactional
    public SolutionRecordUpdateResponse update(
        Long currentUserId,
        Long solutionRecordId,
        SolutionRecordUpdateRequest request
    ) {
        SolutionRecord solutionRecord = getSolutionRecord(solutionRecordId);
        validateAuthor(currentUserId, solutionRecord);

        solutionRecord.update(
            request.title(),
            request.solutionMemo(),
            request.mistakeNote(),
            request.solvingStatus(),
            request.reviewNeeded(),
            request.visibility()
        );

        return SolutionRecordUpdateResponse.from(solutionRecord);
    }

    @Transactional
    public void delete(Long currentUserId, Long solutionRecordId) {
        SolutionRecord solutionRecord = getSolutionRecord(solutionRecordId);
        validateAuthor(currentUserId, solutionRecord);
        solutionRecordRepository.delete(solutionRecord);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private SolutionRecord getSolutionRecord(Long solutionRecordId) {
        return solutionRecordRepository.findById(solutionRecordId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SOLUTION_RECORD_NOT_FOUND));
    }

    private void validateAuthor(Long currentUserId, SolutionRecord solutionRecord) {
        if (!solutionRecord.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateReadable(Long currentUserId, SolutionRecord solutionRecord) {
        if (solutionRecord.getVisibility() == Visibility.PUBLIC) {
            return;
        }
        validateAuthor(currentUserId, solutionRecord);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
