package com.jobintel.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AskAssistantRequest {

    @NotBlank(message = "Prompt question is required")
    private String prompt;
}
