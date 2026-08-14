package com.jobintel.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.job.dto.CreateJobRequest;
import com.jobintel.job.dto.JobDto;
import com.jobintel.resume.PdfExtractionService;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserService userService;
    private final PdfExtractionService pdfExtractionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobService(
            JobRepository jobRepository,
            UserService userService,
            PdfExtractionService pdfExtractionService
    ) {
        this.jobRepository = jobRepository;
        this.userService = userService;
        this.pdfExtractionService = pdfExtractionService;
    }

    @Transactional
    public JobDto createJob(String userEmail, CreateJobRequest request) {
        User user = userService.getEntityByEmail(userEmail);
        String skillsJson = pdfExtractionService.extractSkillsJson(request.getRawText());

        JobDescription job = JobDescription.builder()
                .user(user)
                .companyName(request.getCompanyName().trim())
                .roleTitle(request.getRoleTitle().trim())
                .rawText(request.getRawText().trim())
                .requiredSkillsJson(skillsJson)
                .build();

        JobDescription saved = jobRepository.save(job);
        return mapToDto(saved);
    }

    public List<JobDto> getUserJobs(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        return jobRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobDto getJobById(String userEmail, Long jobId) {
        User user = userService.getEntityByEmail(userEmail);
        JobDescription job = jobRepository.findByIdAndUser(jobId, user)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
        return mapToDto(job);
    }

    public JobDescription getJobEntityById(String userEmail, Long jobId) {
        User user = userService.getEntityByEmail(userEmail);
        return jobRepository.findByIdAndUser(jobId, user)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
    }

    @Transactional
    public void deleteJob(String userEmail, Long jobId) {
        User user = userService.getEntityByEmail(userEmail);
        JobDescription job = jobRepository.findByIdAndUser(jobId, user)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
        jobRepository.delete(job);
    }

    private JobDto mapToDto(JobDescription job) {
        List<String> skills = Collections.emptyList();
        if (job.getRequiredSkillsJson() != null) {
            try {
                skills = objectMapper.readValue(job.getRequiredSkillsJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        return JobDto.builder()
                .id(job.getId())
                .companyName(job.getCompanyName())
                .roleTitle(job.getRoleTitle())
                .rawText(job.getRawText())
                .requiredSkills(skills)
                .createdAt(job.getCreatedAt())
                .build();
    }
}
