package com.jobintel.analytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.analytics.dto.AnalyticsDto;
import com.jobintel.analytics.dto.MissingSkillFrequencyDto;
import com.jobintel.analytics.dto.RoleAnalyticsDto;
import com.jobintel.application.Application;
import com.jobintel.application.ApplicationRepository;
import com.jobintel.application.ApplicationStatus;
import com.jobintel.match.MatchResult;
import com.jobintel.match.MatchResultRepository;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final MatchResultRepository matchResultRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsService(
            ApplicationRepository applicationRepository,
            MatchResultRepository matchResultRepository,
            UserService userService
    ) {
        this.applicationRepository = applicationRepository;
        this.matchResultRepository = matchResultRepository;
        this.userService = userService;
    }

    public AnalyticsDto getUserAnalytics(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        List<Application> applications = applicationRepository.findByUserOrderByAppliedDateDesc(user);
        List<MatchResult> matches = matchResultRepository.findByUserOrderByCreatedAtDesc(user);

        long totalApps = applications.size();
        long interviewCount = applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW || a.getStatus() == ApplicationStatus.OFFER)
                .count();
        long offerCount = applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.OFFER)
                .count();

        double interviewRate = totalApps > 0 ? (double) interviewCount / totalApps * 100.0 : 0.0;
        double offerRate = totalApps > 0 ? (double) offerCount / totalApps * 100.0 : 0.0;

        // Average Match Score
        double avgMatchScore = matches.stream()
                .mapToInt(MatchResult::getScore)
                .average()
                .orElse(0.0);

        // Status Breakdown
        Map<ApplicationStatus, Long> statusBreakdown = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusBreakdown.put(status, 0L);
        }
        for (Application app : applications) {
            statusBreakdown.put(app.getStatus(), statusBreakdown.get(app.getStatus()) + 1);
        }

        // Most Frequently Missing Skills
        Map<String, Long> skillCounts = new HashMap<>();
        for (MatchResult match : matches) {
            if (match.getMissingSkillsJson() != null) {
                try {
                    List<String> missing = objectMapper.readValue(match.getMissingSkillsJson(), new TypeReference<List<String>>() {});
                    for (String skill : missing) {
                        skillCounts.put(skill, skillCounts.getOrDefault(skill, 0L) + 1);
                    }
                } catch (Exception ignored) {}
            }
        }

        List<MissingSkillFrequencyDto> topMissingSkills = skillCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(e -> MissingSkillFrequencyDto.builder().skill(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());

        // Breakdown by Role
        Map<String, List<Application>> appsByRole = applications.stream()
                .collect(Collectors.groupingBy(Application::getRoleTitle));

        List<RoleAnalyticsDto> roleBreakdown = appsByRole.entrySet().stream()
                .map(entry -> {
                    String role = entry.getKey();
                    List<Application> roleApps = entry.getValue();
                    long roleTotal = roleApps.size();
                    long roleInterviews = roleApps.stream()
                            .filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW || a.getStatus() == ApplicationStatus.OFFER)
                            .count();
                    long roleOffers = roleApps.stream()
                            .filter(a -> a.getStatus() == ApplicationStatus.OFFER)
                            .count();
                    double roleInterviewRate = roleTotal > 0 ? (double) roleInterviews / roleTotal * 100.0 : 0.0;

                    return RoleAnalyticsDto.builder()
                            .roleTitle(role)
                            .totalApplications(roleTotal)
                            .interviewCount(roleInterviews)
                            .offerCount(roleOffers)
                            .interviewRate(Math.round(roleInterviewRate * 10.0) / 10.0)
                            .build();
                })
                .sorted((r1, r2) -> Long.compare(r2.getTotalApplications(), r1.getTotalApplications()))
                .collect(Collectors.toList());

        return AnalyticsDto.builder()
                .totalApplications(totalApps)
                .interviewCount(interviewCount)
                .offerCount(offerCount)
                .interviewRate(Math.round(interviewRate * 10.0) / 10.0)
                .offerRate(Math.round(offerRate * 10.0) / 10.0)
                .averageMatchScore(Math.round(avgMatchScore * 10.0) / 10.0)
                .topMissingSkills(topMissingSkills)
                .statusBreakdown(statusBreakdown)
                .roleBreakdown(roleBreakdown)
                .build();
    }
}
