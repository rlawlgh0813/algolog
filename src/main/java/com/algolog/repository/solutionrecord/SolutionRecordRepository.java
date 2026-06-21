package com.algolog.repository.solutionrecord;

import com.algolog.domain.solutionrecord.SolutionRecord;
import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.domain.solutionrecord.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolutionRecordRepository extends JpaRepository<SolutionRecord, Long> {

    @Query("""
        select sr
        from SolutionRecord sr
        join sr.problem p
        where sr.author.id = :authorId
          and (:platform is null or p.platform = :platform)
          and (:difficulty is null or p.difficulty = :difficulty)
          and (:solvingStatus is null or sr.solvingStatus = :solvingStatus)
          and (:reviewNeeded is null or sr.reviewNeeded = :reviewNeeded)
        """)
    Page<SolutionRecord> searchMine(
        @Param("authorId") Long authorId,
        @Param("platform") String platform,
        @Param("difficulty") String difficulty,
        @Param("solvingStatus") SolvingStatus solvingStatus,
        @Param("reviewNeeded") Boolean reviewNeeded,
        Pageable pageable
    );

    @Query("""
        select sr
        from SolutionRecord sr
        join sr.problem p
        where sr.visibility = :visibility
          and (:platform is null or p.platform = :platform)
          and (:difficulty is null or p.difficulty = :difficulty)
          and (:solvingStatus is null or sr.solvingStatus = :solvingStatus)
          and (:reviewNeeded is null or sr.reviewNeeded = :reviewNeeded)
        """)
    Page<SolutionRecord> searchPublic(
        @Param("visibility") Visibility visibility,
        @Param("platform") String platform,
        @Param("difficulty") String difficulty,
        @Param("solvingStatus") SolvingStatus solvingStatus,
        @Param("reviewNeeded") Boolean reviewNeeded,
        Pageable pageable
    );

    @Query("""
        select sr
        from SolutionRecord sr
        where sr.visibility = :visibility
          and sr.problem.id = :problemId
        """)
    Page<SolutionRecord> findPublicByProblemId(
        @Param("visibility") Visibility visibility,
        @Param("problemId") Long problemId,
        Pageable pageable
    );
}
