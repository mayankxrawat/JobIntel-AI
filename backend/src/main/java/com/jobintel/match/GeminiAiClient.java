package com.jobintel.match;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.match.dto.AiMatchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiAiClient implements AiClient {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiAiClient(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-1.5-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public AiMatchResponse analyzeMatch(String resumeText, String jobText) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiForMatch(resumeText, jobText);
            } catch (Exception e) {
                // Log and fallback to grounded deterministic matching engine
            }
        }
        return computeGroundedFallbackMatch(resumeText, jobText);
    }

    @Override
    public List<String> generateResumeImprovements(String resumeText, String jobText, List<String> missingSkills) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiForImprovements(resumeText, jobText, missingSkills);
            } catch (Exception e) {
                // Fallback
            }
        }
        return generateGroundedFallbackImprovements(resumeText, jobText, missingSkills);
    }

    @Override
    public String answerAssistantQuery(String prompt, String groundedContextDataJson) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiForAssistant(prompt, groundedContextDataJson);
            } catch (Exception e) {
                // Fallback
            }
        }
        return generateGroundedFallbackAssistantResponse(prompt, groundedContextDataJson);
    }

    private AiMatchResponse callGeminiForMatch(String resumeText, String jobText) throws Exception {
        String systemInstruction = """
                You are an expert technical recruiter analyzing a resume against a job description.
                STRICT REQUIREMENT: Ground your analysis strictly in the provided resume text and job text.
                Return ONLY a JSON object with keys:
                - "score": integer 0-100 based on actual skill alignment
                - "writtenExplanation": detailed explanation referencing exact text from both documents
                - "strongSkills": array of matching technical skills found in both
                - "missingSkills": array of required skills in job description missing from resume
                - "weakAreas": array of areas where resume has low/partial overlap
                - "improvementSuggestions": array of resume bullet point refinements (DO NOT invent fake experience, fake skills, or fake achievements)
                """;

        String userContent = "RESUME TEXT:\n" + resumeText + "\n\nJOB DESCRIPTION:\n" + jobText;
        String jsonText = executeGeminiPrompt(systemInstruction + "\n\n" + userContent);

        JsonNode root = objectMapper.readTree(jsonText);
        List<String> strong = extractList(root.get("strongSkills"));
        List<String> missing = extractList(root.get("missingSkills"));
        List<String> weak = extractList(root.get("weakAreas"));
        List<String> suggestions = extractList(root.get("improvementSuggestions"));

        return AiMatchResponse.builder()
                .score(root.path("score").asInt(70))
                .writtenExplanation(root.path("writtenExplanation").asText("Match grounded in overlap of skills."))
                .strongSkills(strong)
                .missingSkills(missing)
                .weakAreas(weak)
                .improvementSuggestions(suggestions)
                .build();
    }

    private List<String> callGeminiForImprovements(String resumeText, String jobText, List<String> missingSkills) throws Exception {
        String prompt = "RESUME TEXT:\n" + resumeText + "\n\nJOB TEXT:\n" + jobText +
                "\n\nMISSING SKILLS:\n" + missingSkills +
                "\n\nSuggest 3 concrete resume bullet point improvements. CRITICAL RULE: Never invent experience, skills, or achievements not stated in the resume. Return ONLY a JSON array of strings.";
        String json = executeGeminiPrompt(prompt);
        return extractList(objectMapper.readTree(json));
    }

    private String callGeminiForAssistant(String prompt, String groundedContextDataJson) throws Exception {
        String systemInstruction = """
                You are an AI Career Assistant.
                CRITICAL DIRECTIVE: Answer the candidate's question strictly using their stored application and resume data below.
                If the stored data does not contain enough evidence or applications to answer or infer a pattern, explicitly state that you cannot hallucinate patterns that aren't present in their data.
                """;
        return executeGeminiPrompt(systemInstruction + "\n\nSTORED CANDIDATE DATA:\n" + groundedContextDataJson + "\n\nUSER QUESTION:\n" + prompt);
    }

    private String executeGeminiPrompt(String fullPrompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                )
        );

        String response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode resNode = objectMapper.readTree(response);
        return resNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    // Grounded Fallback Engine when API key is missing or offline
    private AiMatchResponse computeGroundedFallbackMatch(String resumeText, String jobText) {
        Set<String> jobSkills = extractKeywords(jobText);
        Set<String> resumeSkills = extractKeywords(resumeText);

        List<String> strongSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : jobSkills) {
            if (resumeSkills.contains(skill.toLowerCase(Locale.ROOT))) {
                strongSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        int totalRequired = jobSkills.size();
        int score = totalRequired > 0 ? (int) Math.round(((double) strongSkills.size() / totalRequired) * 100) : 75;
        score = Math.min(100, Math.max(10, score));

        List<String> weakAreas = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            weakAreas.add("Absence of explicit keywords for: " + String.join(", ", missingSkills.subList(0, Math.min(3, missingSkills.size()))));
        }
        if (resumeText.length() < 300) {
            weakAreas.add("Resume length is brief; detailed project context could enhance match visibility.");
        }

        String explanation = String.format(
                "Analysis grounded in text matching: Your resume explicitly mentions %d out of %d key technical requirements identified in the job description. Matching skills include [%s]. Missing requirements include [%s].",
                strongSkills.size(),
                totalRequired,
                String.join(", ", strongSkills),
                missingSkills.isEmpty() ? "None" : String.join(", ", missingSkills)
        );

        List<String> improvements = generateGroundedFallbackImprovements(resumeText, jobText, missingSkills);

        return AiMatchResponse.builder()
                .score(score)
                .writtenExplanation(explanation)
                .strongSkills(strongSkills)
                .missingSkills(missingSkills)
                .weakAreas(weakAreas)
                .improvementSuggestions(improvements)
                .build();
    }

    private List<String> generateGroundedFallbackImprovements(String resumeText, String jobText, List<String> missingSkills) {
        List<String> suggestions = new ArrayList<>();

        // Ensure rule: NEVER invent non-stated experience
        if (!missingSkills.isEmpty()) {
            suggestions.add(String.format("If you have practical project experience with %s, highlight specific metric-driven achievements in your work experience section.", missingSkills.get(0)));
        }
        suggestions.add("Reframe existing bullet points to use strong action verbs (e.g. 'Architected', 'Implemented', 'Optimized') and quantify outcomes where possible.");
        suggestions.add("Ensure core technologies explicitly stated in your projects are grouped under a dedicated 'Technical Skills' section for immediate ATS scanning.");

        return suggestions;
    }

    private String generateGroundedFallbackAssistantResponse(String prompt, String groundedContextDataJson) {
        try {
            JsonNode data = objectMapper.readTree(groundedContextDataJson);
            int totalApps = data.path("totalApplications").asInt(0);
            double interviewRate = data.path("interviewRate").asDouble(0.0);
            double offerRate = data.path("offerRate").asDouble(0.0);

            if (totalApps == 0) {
                return "Based on your stored data, you currently have 0 tracked applications. I cannot infer rejection patterns or interview bottlenecks without actual application records. Please add applications in the Application Tracker first.";
            }

            return String.format(
                    "Analysis based on your %d stored application(s):\n" +
                    "- Interview Rate: %.1f%%\n" +
                    "- Offer Rate: %.1f%%\n\n" +
                    "Grounded Recommendation: To improve your conversion rate, focus on applying to roles where your resume match score exceeds 80%% and ensure rejection reasons are logged to distinguish pattern trends.",
                    totalApps, interviewRate, offerRate
            );
        } catch (Exception e) {
            return "Based on your stored data, there are currently insufficient tracked records to infer specific career patterns without speculating. Please log additional application entries.";
        }
    }

    private Set<String> extractKeywords(String text) {
        if (text == null) return Collections.emptySet();
        List<String> keywords = List.of(
                "Java", "Spring Boot", "Spring Data", "Spring Security", "React", "TypeScript",
                "JavaScript", "HTML", "CSS", "SQL", "PostgreSQL", "MySQL", "H2", "Docker",
                "Git", "GitHub", "Maven", "Gradle", "REST API", "Microservices", "JUnit",
                "Mockito", "Python", "Node.js", "AWS", "Azure", "GCP", "Kubernetes", "CI/CD"
        );
        Set<String> found = new LinkedHashSet<>();
        String lower = text.toLowerCase(Locale.ROOT);
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase(Locale.ROOT))) {
                found.add(kw);
            }
        }
        return found;
    }

    private List<String> extractList(JsonNode node) {
        if (node == null || !node.isArray()) return Collections.emptyList();
        List<String> list = new ArrayList<>();
        node.forEach(item -> list.add(item.asText()));
        return list;
    }
}
