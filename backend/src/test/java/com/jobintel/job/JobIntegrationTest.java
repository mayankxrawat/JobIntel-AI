package com.jobintel.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.job.dto.CreateJobRequest;
import com.jobintel.user.UserService;
import com.jobintel.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJobCreationRetrievalAndDelete() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Recruiter Test");
        registerRequest.setEmail("recruiter@example.com");
        registerRequest.setPassword("password123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setCompanyName("Google");
        jobRequest.setRoleTitle("Senior Java Engineer");
        jobRequest.setRawText("We are looking for a Senior Java Developer with Spring Boot, SQL, Docker, and Microservices experience.");

        // 1. Create Job Description
        String createResponse = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("Google"))
                .andExpect(jsonPath("$.roleTitle").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.requiredSkills").isArray())
                .andReturn().getResponse().getContentAsString();

        Long jobId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. Fetch Job Descriptions List
        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(jobId));

        // 3. Delete Job Description
        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Job description deleted successfully"));
    }
}
