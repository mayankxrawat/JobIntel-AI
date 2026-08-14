package com.jobintel.match;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.job.JobService;
import com.jobintel.job.dto.CreateJobRequest;
import com.jobintel.job.dto.JobDto;
import com.jobintel.match.dto.AnalyzeMatchRequest;
import com.jobintel.resume.ResumeService;
import com.jobintel.resume.dto.ResumeDto;
import com.jobintel.user.UserService;
import com.jobintel.user.dto.RegisterRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MatchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private byte[] createSamplePdfBytes(String textContent) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(textContent);
                contentStream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    public void testMatchAnalysisWorkflow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Match Candidate");
        registerRequest.setEmail("candidate@example.com");
        registerRequest.setPassword("password123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        // 1. Upload valid PDF Resume
        byte[] pdfBytes = createSamplePdfBytes("Java Spring Boot REST API PostgreSQL Developer");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample_resume.pdf",
                "application/pdf",
                pdfBytes
        );
        ResumeDto resume = resumeService.uploadResume("candidate@example.com", file);

        // 2. Create Job Description
        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setCompanyName("Amazon");
        jobRequest.setRoleTitle("Backend Engineer");
        jobRequest.setRawText("Looking for a Backend Engineer skilled in Java, Spring Boot, PostgreSQL, and AWS.");
        JobDto job = jobService.createJob("candidate@example.com", jobRequest);

        // 3. Trigger Match Analysis
        AnalyzeMatchRequest analyzeRequest = new AnalyzeMatchRequest();
        analyzeRequest.setResumeId(resume.getId());
        analyzeRequest.setJobId(job.getId());

        String matchResponse = mockMvc.perform(post("/api/matches/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analyzeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.writtenExplanation").exists())
                .andExpect(jsonPath("$.strongSkills").isArray())
                .andExpect(jsonPath("$.missingSkills").isArray())
                .andExpect(jsonPath("$.improvementSuggestions").isArray())
                .andReturn().getResponse().getContentAsString();

        Long matchId = objectMapper.readTree(matchResponse).get("id").asLong();

        // 4. Fetch User Match History
        mockMvc.perform(get("/api/matches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matchId));

        // 5. Delete Match Result
        mockMvc.perform(delete("/api/matches/" + matchId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Match result deleted successfully"));
    }
}
