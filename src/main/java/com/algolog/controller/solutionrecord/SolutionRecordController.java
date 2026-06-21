package com.algolog.controller.solutionrecord;

import com.algolog.domain.solutionrecord.SolvingStatus;
import com.algolog.dto.common.PageResponse;
import com.algolog.dto.solutionrecord.MySolutionRecordResponse;
import com.algolog.dto.solutionrecord.SolutionRecordCreateRequest;
import com.algolog.dto.solutionrecord.SolutionRecordDetailResponse;
import com.algolog.dto.solutionrecord.SolutionRecordResponse;
import com.algolog.dto.solutionrecord.SolutionRecordUpdateRequest;
import com.algolog.dto.solutionrecord.SolutionRecordUpdateResponse;
import com.algolog.service.solutionrecord.SolutionRecordService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SolutionRecordController {

    private final SolutionRecordService solutionRecordService;

    @PostMapping("/api/solution-records")
    public ResponseEntity<SolutionRecordResponse> create(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody SolutionRecordCreateRequest request
    ) {
        SolutionRecordResponse response = solutionRecordService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/solution-records/" + response.id())).body(response);
    }

    @GetMapping("/api/me/solution-records")
    public ResponseEntity<PageResponse<MySolutionRecordResponse>> searchMine(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) String platform,
        @RequestParam(required = false) String difficulty,
        @RequestParam(required = false) SolvingStatus solvingStatus,
        @RequestParam(required = false) Boolean reviewNeeded,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<MySolutionRecordResponse> response = solutionRecordService.searchMine(
            userId,
            platform,
            difficulty,
            solvingStatus,
            reviewNeeded,
            pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/solution-records/{solutionRecordId}")
    public ResponseEntity<SolutionRecordDetailResponse> getById(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long solutionRecordId
    ) {
        SolutionRecordDetailResponse response = solutionRecordService.getById(userId, solutionRecordId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/solution-records/{solutionRecordId}")
    public ResponseEntity<SolutionRecordUpdateResponse> update(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long solutionRecordId,
        @Valid @RequestBody SolutionRecordUpdateRequest request
    ) {
        SolutionRecordUpdateResponse response = solutionRecordService.update(userId, solutionRecordId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/solution-records/{solutionRecordId}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long solutionRecordId
    ) {
        solutionRecordService.delete(userId, solutionRecordId);
        return ResponseEntity.noContent().build();
    }
}
