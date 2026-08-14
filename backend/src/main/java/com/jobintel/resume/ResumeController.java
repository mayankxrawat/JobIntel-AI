package com.jobintel.resume;

import com.jobintel.resume.dto.ResumeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeDto> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(resumeService.uploadResume(authentication.getName(), file));
    }

    @GetMapping
    public ResponseEntity<List<ResumeDto>> getUserResumes(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getUserResumes(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResumeById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(resumeService.getResumeById(authentication.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(
            Authentication authentication,
            @PathVariable Long id
    ) {
        resumeService.deleteResume(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }
}
