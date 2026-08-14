package com.jobintel.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ResumeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

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
    public void testResumePdfUploadAndTextExtraction() throws Exception {
        // Register test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Jane Developer");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("securePass123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        // Create PDF sample bytes containing technical keywords
        byte[] pdfBytes = createSamplePdfBytes("Java Spring Boot Developer with SQL and React experience");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "jane_resume.pdf",
                "application/pdf",
                pdfBytes
        );

        // Upload PDF resume
        String uploadResponse = mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").value("jane_resume.pdf"))
                .andExpect(jsonPath("$.rawText").value("Java Spring Boot Developer with SQL and React experience"))
                .andExpect(jsonPath("$.extractedSkills").isArray())
                .andReturn().getResponse().getContentAsString();

        Long resumeId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // Fetch uploaded resumes list
        mockMvc.perform(get("/api/resumes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resumeId));

        // Delete resume
        mockMvc.perform(delete("/api/resumes/" + resumeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resume deleted successfully"));
    }
}
