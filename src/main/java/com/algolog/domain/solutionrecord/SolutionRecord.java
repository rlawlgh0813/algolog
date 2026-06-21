package com.algolog.domain.solutionrecord;

import com.algolog.domain.common.BaseTimeEntity;
import com.algolog.domain.problem.Problem;
import com.algolog.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "solution_records",
    indexes = {
        @Index(name = "idx_solution_records_author", columnList = "author_id"),
        @Index(name = "idx_solution_records_problem", columnList = "problem_id"),
        @Index(name = "idx_solution_records_visibility", columnList = "visibility")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolutionRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String solutionMemo;

    @Lob
    private String mistakeNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SolvingStatus solvingStatus;

    @Column(nullable = false)
    private boolean reviewNeeded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Builder
    private SolutionRecord(
        User author,
        Problem problem,
        String title,
        String solutionMemo,
        String mistakeNote,
        SolvingStatus solvingStatus,
        boolean reviewNeeded,
        Visibility visibility
    ) {
        this.author = author;
        this.problem = problem;
        this.title = title;
        this.solutionMemo = solutionMemo;
        this.mistakeNote = mistakeNote;
        this.solvingStatus = solvingStatus;
        this.reviewNeeded = reviewNeeded;
        this.visibility = visibility;
    }

    public void update(
        String title,
        String solutionMemo,
        String mistakeNote,
        SolvingStatus solvingStatus,
        boolean reviewNeeded,
        Visibility visibility
    ) {
        this.title = title;
        this.solutionMemo = solutionMemo;
        this.mistakeNote = mistakeNote;
        this.solvingStatus = solvingStatus;
        this.reviewNeeded = reviewNeeded;
        this.visibility = visibility;
    }
}
