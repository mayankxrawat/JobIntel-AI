package com.jobintel.match;

import com.jobintel.match.dto.AiMatchResponse;

import java.util.List;

public interface AiClient {
    AiMatchResponse analyzeMatch(String resumeText, String jobText);
    List<String> generateResumeImprovements(String resumeText, String jobText, List<String> missingSkills);
    String answerAssistantQuery(String prompt, String groundedContextDataJson);
}
