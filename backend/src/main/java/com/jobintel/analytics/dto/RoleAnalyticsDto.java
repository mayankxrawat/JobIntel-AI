package com.jobintel.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAnalyticsDto {
    private String roleTitle;
    private long totalApplications;
    private long interviewCount;
    private long offerCount;
    private double interviewRate;
}
