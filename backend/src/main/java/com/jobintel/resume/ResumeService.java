package com.jobintel.resume;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.resume.dto.ResumeDto;
import com.jobintel.user.User;
import com.jobintel.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PdfExtractionService pdfExtractionService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeService(
            ResumeRepository resumeRepository,
            PdfExtractionService pdfExtractionService,
            UserService userService
    ) {
        this.resumeRepository = resumeRepository;
        this.pdfExtractionService = pdfExtractionService;
        this.userService = userService;
    }

    @Transactional
    public ResumeDto uploadResume(String userEmail, MultipartFile file) throws IOException {
        User user = userService.getEntityByEmail(userEmail);
        String rawText = pdfExtractionService.extractText(file);
        String skillsJson = pdfExtractionService.extractSkillsJson(rawText);

        Resume resume = Resume.builder()
                .user(user)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf")
                .rawText(rawText)
                .extractedSkillsJson(skillsJson)
                .build();

        Resume saved = resumeRepository.save(resume);
        return mapToDto(saved);
    }

    public List<ResumeDto> getUserResumes(String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        return resumeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ResumeDto getResumeById(String userEmail, Long resumeId) {
        User user = userService.getEntityByEmail(userEmail);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        return mapToDto(resume);
    }

    public Resume getResumeEntityById(String userEmail, Long resumeId) {
        User user = userService.getEntityByEmail(userEmail);
        return resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
    }

    @Transactional
    public void deleteResume(String userEmail, Long resumeId) {
        User user = userService.getEntityByEmail(userEmail);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        resumeRepository.delete(resume);
    }

    private ResumeDto mapToDto(Resume resume) {
        List<String> skills = Collections.emptyList();
        if (resume.getExtractedSkillsJson() != null) {
            try {
                skills = objectMapper.readValue(resume.getExtractedSkillsJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        return ResumeDto.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .rawText(resume.getRawText())
                .extractedSkills(skills)
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
