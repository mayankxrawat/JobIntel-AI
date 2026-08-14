package com.jobintel.match.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyzeMatchRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotNull(message = "Job description ID is required")
    private Long jobId;
}
