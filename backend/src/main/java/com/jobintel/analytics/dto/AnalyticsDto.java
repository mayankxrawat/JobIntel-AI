package com.jobintel.analytics.dto;

import com.jobintel.application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private long totalApplications;
    private long interviewCount;
    private long offerCount;
    private double interviewRate;
    private double offerRate;
    private double averageMatchScore;
    private List<MissingSkillFrequencyDto> topMissingSkills;
    private Map<ApplicationStatus, Long> statusBreakdown;
    private List<RoleAnalyticsDto> roleBreakdown;
}
