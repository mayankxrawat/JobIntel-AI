package com.jobintel.application.dto;

import com.jobintel.application.ApplicationStatus;
import com.jobintel.application.RejectionReasonSource;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateApplicationRequest {

    private Long jobId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role title is required")
    private String roleTitle;

    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private RejectionReasonSource rejectionReasonSource = RejectionReasonSource.NONE;

    private String rejectionDetails;

    private LocalDate appliedDate;

    private String notes;
}
