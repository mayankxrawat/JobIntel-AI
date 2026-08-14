package com.jobintel.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.application.dto.CreateApplicationRequest;
import com.jobintel.application.dto.UpdateApplicationRequest;
import com.jobintel.user.UserService;
import com.jobintel.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testApplicationTrackingCrudWorkflow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Tracker User");
        registerRequest.setEmail("tracker@example.com");
        registerRequest.setPassword("password123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        // 1. Create Application
        CreateApplicationRequest createRequest = new CreateApplicationRequest();
        createRequest.setCompanyName("Microsoft");
        createRequest.setRoleTitle("Full Stack Developer");
        createRequest.setStatus(ApplicationStatus.APPLIED);
        createRequest.setAppliedDate(LocalDate.now());

        String createResponse = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("Microsoft"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.rejectionReasonSource").value("NONE"))
                .andReturn().getResponse().getContentAsString();

        Long appId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. Update Application status to REJECTED with EMPLOYER_STATED
        UpdateApplicationRequest updateRequest = new UpdateApplicationRequest();
        updateRequest.setStatus(ApplicationStatus.REJECTED);
        updateRequest.setRejectionReasonSource(RejectionReasonSource.EMPLOYER_STATED);
        updateRequest.setRejectionDetails("Employer stated position filled by internal candidate");

        mockMvc.perform(put("/api/applications/" + appId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReasonSource").value("EMPLOYER_STATED"))
                .andExpect(jsonPath("$.rejectionDetails").value("Employer stated position filled by internal candidate"));

        // 3. Fetch User Applications
        mockMvc.perform(get("/api/applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(appId));

        // 4. Delete Application
        mockMvc.perform(delete("/api/applications/" + appId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Application deleted successfully"));
    }
}
