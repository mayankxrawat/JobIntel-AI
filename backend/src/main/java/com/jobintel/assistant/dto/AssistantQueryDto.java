package com.jobintel.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantQueryDto {
    private Long id;
    private String prompt;
    private String response;
    private String groundingDataJson;
    private Instant createdAt;
}
