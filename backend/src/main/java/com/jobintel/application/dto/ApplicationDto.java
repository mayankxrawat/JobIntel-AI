package com.jobintel.application.dto;

import com.jobintel.application.ApplicationStatus;
import com.jobintel.application.RejectionReasonSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private Long jobId;
    private String companyName;
    private String roleTitle;
    private ApplicationStatus status;
    private RejectionReasonSource rejectionReasonSource;
    private String rejectionDetails;
    private LocalDate appliedDate;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
