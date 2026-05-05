package com.algolog.domain.problem;

import com.algolog.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "problems",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_problems_platform_problem_number",
            columnNames = {"platform", "problem_number"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String platform;

    @Column(name = "problem_number", nullable = false, length = 50)
    private String problemNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String difficulty;

    @Builder
    private Problem(String platform, String problemNumber, String title, String difficulty) {
        this.platform = platform;
        this.problemNumber = problemNumber;
        this.title = title;
        this.difficulty = difficulty;
    }
}
