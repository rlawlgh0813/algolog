package com.algolog.service.counterexample;

import com.algolog.domain.counterexample.CounterExample;
import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.Visibility;
import com.algolog.dto.counterexample.CounterExampleCreateRequest;
import com.algolog.dto.counterexample.CounterExampleResponse;
import com.algolog.global.exception.BusinessException;
import com.algolog.global.exception.ErrorCode;
import com.algolog.repository.counterexample.CounterExampleRepository;
import com.algolog.repository.solutionrecord.SolutionRecordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterExampleService {

    private final CounterExampleRepository counterExampleRepository;
    private final SolutionRecordRepository solutionRecordRepository;

    @Transactional
    public CounterExampleResponse create(
        Long currentUserId,
        Long solutionRecordId,
        CounterExampleCreateRequest request
    ) {
        SolutionRecord solutionRecord = getSolutionRecord(solutionRecordId);
        validateAuthor(currentUserId, solutionRecord);

        CounterExample counterExample = CounterExample.builder()
            .solutionRecord(solutionRecord)
            .inputExample(request.inputExample())
            .expectedBehavior(request.expectedBehavior())
            .wrongReason(request.wrongReason())
            .fixMemo(request.fixMemo())
            .build();

        return CounterExampleResponse.from(counterExampleRepository.save(counterExample));
    }

    @Transactional(readOnly = true)
    public List<CounterExampleResponse> getBySolutionRecord(
        Long currentUserId,
        Long solutionRecordId
    ) {
        SolutionRecord solutionRecord = getSolutionRecord(solutionRecordId);
        validateReadable(currentUserId, solutionRecord);

        return counterExampleRepository.findAllBySolutionRecordIdOrderByCreatedAtAsc(solutionRecordId)
            .stream()
            .map(CounterExampleResponse::from)
            .toList();
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
}
