package com.algolog.controller.problem;

import com.algolog.dto.common.PageResponse;
import com.algolog.dto.problem.ProblemCreateRequest;
import com.algolog.dto.problem.ProblemResponse;
import com.algolog.service.problem.ProblemService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<ProblemResponse> create(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = problemService.create(request);
        return ResponseEntity.created(URI.create("/api/problems/" + response.id())).body(response);
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ProblemResponse> getById(@PathVariable Long problemId) {
        ProblemResponse response = problemService.getById(problemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProblemResponse>> search(
        @RequestParam(required = false) String platform,
        @RequestParam(required = false) String difficulty,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<ProblemResponse> response = problemService.search(
            platform,
            difficulty,
            keyword,
            pageable
        );
        return ResponseEntity.ok(response);
    }
}
