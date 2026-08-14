package com.jobintel.application;

import com.jobintel.application.dto.ApplicationDto;
import com.jobintel.application.dto.CreateApplicationRequest;
import com.jobintel.application.dto.UpdateApplicationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> createApplication(
            Authentication authentication,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.ok(applicationService.createApplication(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getUserApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getUserApplications(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> getApplicationById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(applicationService.getApplicationById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDto> updateApplication(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateApplicationRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateApplication(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteApplication(
            Authentication authentication,
            @PathVariable Long id
    ) {
        applicationService.deleteApplication(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
    }
}
