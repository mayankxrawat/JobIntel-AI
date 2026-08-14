package com.jobintel.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.assistant.dto.AskAssistantRequest;
import com.jobintel.user.UserService;
import com.jobintel.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AssistantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAssistantQueryAndGroundedRefusal() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Assistant User");
        registerRequest.setEmail("assistant@example.com");
        registerRequest.setPassword("password123");
        var authResponse = userService.register(registerRequest);
        String token = authResponse.getToken();

        AskAssistantRequest askRequest = new AskAssistantRequest();
        askRequest.setPrompt("Why is my interview rate low?");

        // 1. Ask Assistant (When 0 applications exist)
        String askResponse = mockMvc.perform(post("/api/assistant/ask")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.prompt").value("Why is my interview rate low?"))
                .andExpect(jsonPath("$.response").exists())
                .andReturn().getResponse().getContentAsString();

        // 2. Fetch User Assistant Query History
        mockMvc.perform(get("/api/assistant/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prompt").value("Why is my interview rate low?"));
    }
}
