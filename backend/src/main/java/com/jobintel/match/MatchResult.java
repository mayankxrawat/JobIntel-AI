package com.jobintel.match;

import com.jobintel.job.JobDescription;
import com.jobintel.resume.Resume;
import com.jobintel.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobDescription jobDescription;

    @Column(nullable = false)
    private int score;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String writtenExplanation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strongSkillsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String missingSkillsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String weakAreasJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String improvementSuggestionsJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
