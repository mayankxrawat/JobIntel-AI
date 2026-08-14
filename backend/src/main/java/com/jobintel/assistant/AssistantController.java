package com.jobintel.assistant;

import com.jobintel.assistant.dto.AskAssistantRequest;
import com.jobintel.assistant.dto.AssistantQueryDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AssistantQueryDto> askAssistant(
            Authentication authentication,
            @Valid @RequestBody AskAssistantRequest request
    ) {
        return ResponseEntity.ok(assistantService.askAssistant(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AssistantQueryDto>> getUserHistory(Authentication authentication) {
        return ResponseEntity.ok(assistantService.getUserHistory(authentication.getName()));
    }
}
