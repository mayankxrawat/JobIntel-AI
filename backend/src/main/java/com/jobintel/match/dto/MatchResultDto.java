package com.jobintel.match.dto;

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
public class MatchResultDto {
    private Long id;
    private Long resumeId;
    private String resumeFileName;
    private Long jobId;
    private String companyName;
    private String roleTitle;
    private int score;
    private String writtenExplanation;
    private List<String> strongSkills;
    private List<String> missingSkills;
    private List<String> weakAreas;
    private List<String> improvementSuggestions;
    private Instant createdAt;
}
