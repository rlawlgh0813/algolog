package com.algolog.repository.counterexample;

import com.algolog.domain.counterexample.CounterExample;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterExampleRepository extends JpaRepository<CounterExample, Long> {

    List<CounterExample> findAllBySolutionRecordIdOrderByCreatedAtAsc(Long solutionRecordId);

    void deleteAllBySolutionRecordId(Long solutionRecordId);
}
