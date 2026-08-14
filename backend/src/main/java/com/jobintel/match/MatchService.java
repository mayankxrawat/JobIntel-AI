package com.jobintel.match;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.job.JobDescription;
import com.jobintel.job.JobService;
import com.jobintel.match.dto.AiMatchResponse;
import com.jobintel.match.dto.AnalyzeMatchRequest;
import com.jobintel.match.dto.MatchResultDto;
import com.jobintel.resume.Resume;
import com.jobintel.resume.ResumeService;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchResultRepository matchResultRepository;
    private final UserService userService;
    private final ResumeService resumeService;
    private final JobService jobService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchService(
            MatchResultRepository matchResultRepository,
            UserService userService,
            ResumeService resumeService,
            JobService jobService,
            AiClient aiClient
    ) {
        this.matchResultRepository = matchResultRepository;
        this.userService = userService;
        this.resumeService = resumeService;
        this.jobService = jobService;
        this.aiClient = aiClient;
    }

    @Transactional
    public MatchResultDto analyzeAndSaveMatch(String userEmail, AnalyzeMatchRequest request) {
        User user = userService.getEntityByEmail(userEmail);
        Resume resume = resumeService.getResumeEntityById(userEmail, request.getResumeId());
        JobDescription job = jobService.getJobEntityById(userEmail, request.getJobId());

        AiMatchResponse aiResponse = aiClient.analyzeMatch(resume.getRawText(), job.getRawText());

        MatchResult matchResult = MatchResult.builder()
                .user(user)
                .resume(resume)
                .jobDescription(job)
                .score(aiResponse.getScore())
                .writtenExplanation(aiResponse.getWrittenExplanation())
                .strongSkillsJson(toJson(aiResponse.getStrongSkills()))
                .missingSkillsJson(toJson(aiResponse.getMissingSkills()))
                .weakAreasJson(toJson(aiResponse.getWeakAreas()))
                .improvementSuggestionsJson(toJson(aiResponse.getImprovementSuggestions()))
                .build();

        MatchResult saved = matchResultRepository.save(matchResult);
        return mapToDto(saved);
    }

    public List<MatchResultDto> getUserMatches(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        return matchResultRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public MatchResultDto getMatchById(String userEmail, Long matchId) {
        User user = userService.getEntityByEmail(userEmail);
        MatchResult matchResult = matchResultRepository.findByIdAndUser(matchId, user)
                .orElseThrow(() -> new IllegalArgumentException("Match result not found"));
        return mapToDto(matchResult);
    }

    @Transactional
    public void deleteMatch(String userEmail, Long matchId) {
        User user = userService.getEntityByEmail(userEmail);
        MatchResult matchResult = matchResultRepository.findByIdAndUser(matchId, user)
                .orElseThrow(() -> new IllegalArgumentException("Match result not found"));
        matchResultRepository.delete(matchResult);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public MatchResultDto mapToDto(MatchResult match) {
        return MatchResultDto.builder()
                .id(match.getId())
                .resumeId(match.getResume().getId())
                .resumeFileName(match.getResume().getFileName())
                .jobId(match.getJobDescription().getId())
                .companyName(match.getJobDescription().getCompanyName())
                .roleTitle(match.getJobDescription().getRoleTitle())
                .score(match.getScore())
                .writtenExplanation(match.getWrittenExplanation())
                .strongSkills(fromJson(match.getStrongSkillsJson()))
                .missingSkills(fromJson(match.getMissingSkillsJson()))
                .weakAreas(fromJson(match.getWeakAreasJson()))
                .improvementSuggestions(fromJson(match.getImprovementSuggestionsJson()))
                .createdAt(match.getCreatedAt())
                .build();
    }
}
