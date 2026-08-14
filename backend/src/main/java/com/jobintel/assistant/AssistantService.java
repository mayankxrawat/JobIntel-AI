package com.jobintel.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.analytics.AnalyticsService;
import com.jobintel.analytics.dto.AnalyticsDto;
import com.jobintel.application.ApplicationService;
import com.jobintel.application.dto.ApplicationDto;
import com.jobintel.assistant.dto.AskAssistantRequest;
import com.jobintel.assistant.dto.AssistantQueryDto;
import com.jobintel.match.AiClient;
import com.jobintel.match.MatchService;
import com.jobintel.match.dto.MatchResultDto;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private final AssistantQueryRepository assistantQueryRepository;
    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final ApplicationService applicationService;
    private final MatchService matchService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AssistantService(
            AssistantQueryRepository assistantQueryRepository,
            UserService userService,
            AnalyticsService analyticsService,
            ApplicationService applicationService,
            MatchService matchService,
            AiClient aiClient
    ) {
        this.assistantQueryRepository = assistantQueryRepository;
        this.userService = userService;
        this.analyticsService = analyticsService;
        this.applicationService = applicationService;
        this.matchService = matchService;
        this.aiClient = aiClient;
    }

    @Transactional
    public AssistantQueryDto askAssistant(String userEmail, AskAssistantRequest request) {
        User user = userService.getEntityByEmail(userEmail);
        AnalyticsDto analytics = analyticsService.getUserAnalytics(userEmail);
        List<ApplicationDto> applications = applicationService.getUserApplications(userEmail);
        List<MatchResultDto> matches = matchService.getUserMatches(userEmail);

        // Build grounded context data package
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("totalApplications", analytics.getTotalApplications());
        contextMap.put("interviewCount", analytics.getInterviewCount());
        contextMap.put("offerCount", analytics.getOfferCount());
        contextMap.put("interviewRate", analytics.getInterviewRate());
        contextMap.put("offerRate", analytics.getOfferRate());
        contextMap.put("averageMatchScore", analytics.getAverageMatchScore());
        contextMap.put("topMissingSkills", analytics.getTopMissingSkills());
        contextMap.put("roleBreakdown", analytics.getRoleBreakdown());

        contextMap.put("recentApplications", applications.stream().limit(10).map(app -> Map.of(
                "company", app.getCompanyName(),
                "role", app.getRoleTitle(),
                "status", app.getStatus().name(),
                "rejectionSource", app.getRejectionReasonSource().name(),
                "rejectionDetails", app.getRejectionDetails() != null ? app.getRejectionDetails() : ""
        )).collect(Collectors.toList()));

        contextMap.put("recentMatches", matches.stream().limit(5).map(m -> Map.of(
                "company", m.getCompanyName(),
                "role", m.getRoleTitle(),
                "score", m.getScore(),
                "missingSkills", m.getMissingSkills()
        )).collect(Collectors.toList()));

        String contextJson;
        try {
            contextJson = objectMapper.writeValueAsString(contextMap);
        } catch (Exception e) {
            contextJson = "{}";
        }

        String aiAnswer = aiClient.answerAssistantQuery(request.getPrompt(), contextJson);

        AssistantQuery query = AssistantQuery.builder()
                .user(user)
                .prompt(request.getPrompt().trim())
                .response(aiAnswer)
                .groundingDataJson(contextJson)
                .build();

        AssistantQuery saved = assistantQueryRepository.save(query);
        return mapToDto(saved);
    }

    public List<AssistantQueryDto> getUserHistory(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        return assistantQueryRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AssistantQueryDto mapToDto(AssistantQuery query) {
        return AssistantQueryDto.builder()
                .id(query.getId())
                .prompt(query.getPrompt())
                .response(query.getResponse())
                .groundingDataJson(query.getGroundingDataJson())
                .createdAt(query.getCreatedAt())
                .build();
    }
}
