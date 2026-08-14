package com.jobintel.assistant;

import com.jobintel.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "assistant_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String groundingDataJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
