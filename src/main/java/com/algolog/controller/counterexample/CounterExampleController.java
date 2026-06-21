package com.algolog.controller.counterexample;

import com.algolog.dto.counterexample.CounterExampleCreateRequest;
import com.algolog.dto.counterexample.CounterExampleResponse;
import com.algolog.service.counterexample.CounterExampleService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CounterExampleController {

    private final CounterExampleService counterExampleService;

    @PostMapping("/api/solution-records/{solutionRecordId}/counter-examples")
    public ResponseEntity<CounterExampleResponse> create(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long solutionRecordId,
        @Valid @RequestBody CounterExampleCreateRequest request
    ) {
        CounterExampleResponse response = counterExampleService.create(userId, solutionRecordId, request);
        return ResponseEntity.created(URI.create(
            "/api/solution-records/" + solutionRecordId + "/counter-examples/" + response.id()
        )).body(response);
    }

    @GetMapping("/api/solution-records/{solutionRecordId}/counter-examples")
    public ResponseEntity<List<CounterExampleResponse>> getBySolutionRecord(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long solutionRecordId
    ) {
        List<CounterExampleResponse> response = counterExampleService.getBySolutionRecord(
            userId,
            solutionRecordId
        );
        return ResponseEntity.ok(response);
    }
}
