package com.jobintel.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResponse {
    private int score;
    private String writtenExplanation;
    private List<String> strongSkills;
    private List<String> missingSkills;
    private List<String> weakAreas;
    private List<String> improvementSuggestions;
}
