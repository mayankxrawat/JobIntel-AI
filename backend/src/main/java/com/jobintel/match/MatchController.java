package com.jobintel.match;

import com.jobintel.match.dto.AnalyzeMatchRequest;
import com.jobintel.match.dto.MatchResultDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<MatchResultDto> analyzeMatch(
            Authentication authentication,
            @Valid @RequestBody AnalyzeMatchRequest request
    ) {
        return ResponseEntity.ok(matchService.analyzeAndSaveMatch(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<MatchResultDto>> getUserMatches(Authentication authentication) {
        return ResponseEntity.ok(matchService.getUserMatches(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResultDto> getMatchById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(matchService.getMatchById(authentication.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMatch(
            Authentication authentication,
            @PathVariable Long id
    ) {
        matchService.deleteMatch(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Match result deleted successfully"));
    }
}
