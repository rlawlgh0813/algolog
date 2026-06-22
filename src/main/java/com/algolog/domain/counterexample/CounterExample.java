package com.algolog.domain.counterexample;

import com.algolog.domain.common.BaseTimeEntity;
import com.algolog.domain.solutionrecord.SolutionRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "counter_examples",
    indexes = {
        @Index(name = "idx_counter_examples_solution_record", columnList = "solution_record_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounterExample extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_record_id", nullable = false)
    private SolutionRecord solutionRecord;

    @Lob
    @Column(nullable = false, length = 65_535)
    private String inputExample;

    @Lob
    private String expectedBehavior;

    @Lob
    private String wrongReason;

    @Lob
    private String fixMemo;

    @Builder
    private CounterExample(
        SolutionRecord solutionRecord,
        String inputExample,
        String expectedBehavior,
        String wrongReason,
        String fixMemo
    ) {
        this.solutionRecord = solutionRecord;
        this.inputExample = inputExample;
        this.expectedBehavior = expectedBehavior;
        this.wrongReason = wrongReason;
        this.fixMemo = fixMemo;
    }
}
