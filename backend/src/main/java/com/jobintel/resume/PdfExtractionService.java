package com.jobintel.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Service
public class PdfExtractionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> COMMON_SKILLS = List.of(
            "Java", "Spring Boot", "Spring Data", "Spring Security", "React", "TypeScript",
            "JavaScript", "HTML", "CSS", "SQL", "PostgreSQL", "MySQL", "H2", "Docker",
            "Git", "GitHub", "Maven", "Gradle", "REST API", "Microservices", "JUnit",
            "Mockito", "Python", "Node.js", "C++", "C#", ".NET", "AWS", "Azure",
            "GCP", "Kubernetes", "Linux", "CI/CD", "Hibernate", "JPA", "Kafka",
            "Redis", "GraphQL", "Tailwind", "Bootstrap", "Agile", "Scrum"
    );

    public String extractText(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Could not extract readable text from PDF. The document might be image-based or protected.");
            }
            return text.trim();
        }
    }

    public String extractSkillsJson(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "[]";
        }

        String lowerText = rawText.toLowerCase(Locale.ROOT);
        Set<String> matchedSkills = new TreeSet<>();

        for (String skill : COMMON_SKILLS) {
            String regex = "(?i)\\b" + java.util.regex.Pattern.quote(skill) + "\\b";
            if (lowerText.matches(".*" + regex + ".*") || lowerText.contains(skill.toLowerCase(Locale.ROOT))) {
                matchedSkills.add(skill);
            }
        }

        try {
            return objectMapper.writeValueAsString(new ArrayList<>(matchedSkills));
        } catch (Exception e) {
            return "[]";
        }
    }
}
