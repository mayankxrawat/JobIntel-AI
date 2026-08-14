package com.jobintel.application;

import com.jobintel.application.dto.ApplicationDto;
import com.jobintel.application.dto.CreateApplicationRequest;
import com.jobintel.application.dto.UpdateApplicationRequest;
import com.jobintel.job.JobDescription;
import com.jobintel.job.JobService;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final JobService jobService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserService userService,
            JobService jobService
    ) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
        this.jobService = jobService;
    }

    @Transactional
    public ApplicationDto createApplication(String userEmail, CreateApplicationRequest request) {
        User user = userService.getEntityByEmail(userEmail);
        JobDescription job = null;

        if (request.getJobId() != null) {
            try {
                job = jobService.getJobEntityById(userEmail, request.getJobId());
            } catch (Exception ignored) {}
        }

        Application application = Application.builder()
                .user(user)
                .jobDescription(job)
                .companyName(request.getCompanyName().trim())
                .roleTitle(request.getRoleTitle().trim())
                .status(request.getStatus() != null ? request.getStatus() : ApplicationStatus.APPLIED)
                .rejectionReasonSource(request.getRejectionReasonSource() != null ? request.getRejectionReasonSource() : RejectionReasonSource.NONE)
                .rejectionDetails(request.getRejectionDetails())
                .appliedDate(request.getAppliedDate() != null ? request.getAppliedDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        Application saved = applicationRepository.save(application);
        return mapToDto(saved);
    }

    public List<ApplicationDto> getUserApplications(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        return applicationRepository.findByUserOrderByAppliedDateDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ApplicationDto getApplicationById(String userEmail, Long applicationId) {
        User user = userService.getEntityByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        return mapToDto(application);
    }

    @Transactional
    public ApplicationDto updateApplication(String userEmail, Long applicationId, UpdateApplicationRequest request) {
        User user = userService.getEntityByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (request.getCompanyName() != null) application.setCompanyName(request.getCompanyName().trim());
        if (request.getRoleTitle() != null) application.setRoleTitle(request.getRoleTitle().trim());
        if (request.getStatus() != null) application.setStatus(request.getStatus());
        if (request.getRejectionReasonSource() != null) application.setRejectionReasonSource(request.getRejectionReasonSource());
        if (request.getRejectionDetails() != null) application.setRejectionDetails(request.getRejectionDetails());
        if (request.getAppliedDate() != null) application.setAppliedDate(request.getAppliedDate());
        if (request.getNotes() != null) application.setNotes(request.getNotes());

        Application updated = applicationRepository.save(application);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteApplication(String userEmail, Long applicationId) {
        User user = userService.getEntityByEmail(userEmail);
        Application application = applicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        applicationRepository.delete(application);
    }

    public ApplicationDto mapToDto(Application app) {
        return ApplicationDto.builder()
                .id(app.getId())
                .jobId(app.getJobDescription() != null ? app.getJobDescription().getId() : null)
                .companyName(app.getCompanyName())
                .roleTitle(app.getRoleTitle())
                .status(app.getStatus())
                .rejectionReasonSource(app.getRejectionReasonSource())
                .rejectionDetails(app.getRejectionDetails())
                .appliedDate(app.getAppliedDate())
                .notes(app.getNotes())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
