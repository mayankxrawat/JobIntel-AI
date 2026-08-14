package com.jobintel.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private String companyName;
    private String roleTitle;
    private String rawText;
    private List<String> requiredSkills;
    private Instant createdAt;
}
