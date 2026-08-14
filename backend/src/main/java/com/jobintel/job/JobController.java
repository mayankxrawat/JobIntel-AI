package com.jobintel.job;

import com.jobintel.job.dto.CreateJobRequest;
import com.jobintel.job.dto.JobDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobDto> createJob(
            Authentication authentication,
            @Valid @RequestBody CreateJobRequest request
    ) {
        return ResponseEntity.ok(jobService.createJob(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getUserJobs(Authentication authentication) {
        return ResponseEntity.ok(jobService.getUserJobs(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(jobService.getJobById(authentication.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(
            Authentication authentication,
            @PathVariable Long id
    ) {
        jobService.deleteJob(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Job description deleted successfully"));
    }
}
