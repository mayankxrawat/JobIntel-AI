package com.jobintel.application.dto;

import com.jobintel.application.ApplicationStatus;
import com.jobintel.application.RejectionReasonSource;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateApplicationRequest {

    private String companyName;
    private String roleTitle;
    private ApplicationStatus status;
    private RejectionReasonSource rejectionReasonSource;
    private String rejectionDetails;
    private LocalDate appliedDate;
    private String notes;
}
