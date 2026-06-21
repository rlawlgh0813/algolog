package com.algolog.service.problem;

import com.algolog.domain.problem.Problem;
import com.algolog.dto.common.PageResponse;
import com.algolog.dto.problem.ProblemCreateRequest;
import com.algolog.dto.problem.ProblemResponse;
import com.algolog.global.exception.BusinessException;
import com.algolog.global.exception.ErrorCode;
import com.algolog.repository.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    @Transactional
    public ProblemResponse create(ProblemCreateRequest request) {
        validateDuplicateProblem(request.platform(), request.problemNumber());

        Problem problem = Problem.builder()
            .platform(request.platform())
            .problemNumber(request.problemNumber())
            .title(request.title())
            .difficulty(request.difficulty())
            .build();

        try {
            Problem savedProblem = problemRepository.save(problem);
            return ProblemResponse.from(savedProblem);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROBLEM);
        }
    }

    @Transactional(readOnly = true)
    public ProblemResponse getById(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        return ProblemResponse.from(problem);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProblemResponse> search(
        String platform,
        String difficulty,
        String keyword,
        Pageable pageable
    ) {
        return PageResponse.from(problemRepository.search(
            normalize(platform),
            normalize(difficulty),
            normalize(keyword),
            pageable
        ).map(ProblemResponse::from));
    }

    private void validateDuplicateProblem(String platform, String problemNumber) {
        if (problemRepository.existsByPlatformAndProblemNumber(platform, problemNumber)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROBLEM);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
