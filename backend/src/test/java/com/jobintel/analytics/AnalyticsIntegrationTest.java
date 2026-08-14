package com.jobintel.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.application.ApplicationService;
import com.jobintel.application.ApplicationStatus;
import com.jobintel.application.dto.CreateApplicationRequest;
import com.jobintel.user.UserService;
import com.jobintel.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationService applicationService;

    @Test
    public void testAnalyticsCalculationFromRealData() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Analytics Candidate");
        registerRequest.setEmail("analytics@example.com");
        registerRequest.setPassword("password123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        // 1. Add 2 Applications (1 APPLIED, 1 INTERVIEW)
        CreateApplicationRequest app1 = new CreateApplicationRequest();
        app1.setCompanyName("Company A");
        app1.setRoleTitle("Backend Developer");
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setAppliedDate(LocalDate.now());
        applicationService.createApplication("analytics@example.com", app1);

        CreateApplicationRequest app2 = new CreateApplicationRequest();
        app2.setCompanyName("Company B");
        app2.setRoleTitle("Backend Developer");
        app2.setStatus(ApplicationStatus.INTERVIEW);
        app2.setAppliedDate(LocalDate.now());
        applicationService.createApplication("analytics@example.com", app2);

        // 2. Fetch Analytics endpoint
        mockMvc.perform(get("/api/analytics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(2))
                .andExpect(jsonPath("$.interviewCount").value(1))
                .andExpect(jsonPath("$.interviewRate").value(50.0))
                .andExpect(jsonPath("$.offerRate").value(0.0))
                .andExpect(jsonPath("$.roleBreakdown[0].roleTitle").value("Backend Developer"))
                .andExpect(jsonPath("$.roleBreakdown[0].totalApplications").value(2));
    }
}
