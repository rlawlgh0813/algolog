package com.algolog.repository.problem;

import com.algolog.domain.problem.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsByPlatformAndProblemNumber(String platform, String problemNumber);

    @Query("""
        select p
        from Problem p
        where (:platform is null or p.platform = :platform)
          and (:difficulty is null or p.difficulty = :difficulty)
          and (
            :keyword is null
            or lower(p.problemNumber) like lower(concat('%', :keyword, '%'))
            or lower(p.title) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<Problem> search(
        @Param("platform") String platform,
        @Param("difficulty") String difficulty,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
