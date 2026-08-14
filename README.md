# AI Job Application Intelligence Platform

A portfolio project designed for **Java / Spring Boot** software engineering positions. Built using a **Modular Monolith** architecture (Package-by-Feature), **React + TypeScript**, **PostgreSQL / Spring Data JPA**, **Spring Security + JWT**, **Apache PDFBox**, and **Data-Grounded AI Matching & Analytics**.

---

## Technical Stack & Architecture

- **Backend Framework**: Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security.
- **Architecture Pattern**: Modular Monolith (**Package-by-Feature** layout):
  - `com.jobintel.user`: JWT authentication, user registration & login.
  - `com.jobintel.resume`: Resume entity & Apache PDFBox text extraction.
  - `com.jobintel.job`: Job description storage & keyword parsing.
  - `com.jobintel.match`: `AiClient` interface abstraction, Gemini / OpenAI matching engine, grounded scoring & resume improvement suggestions without hallucinated experience.
  - `com.jobintel.application`: Application status pipeline tracking (`APPLIED`, `INTERVIEW`, `REJECTED`, `OFFER`) and explicit objective rejection attribution (`EMPLOYER_STATED`, `PATTERN_INFERRED`, `AI_SPECULATED`, `NONE`).
  - `com.jobintel.analytics`: SQL/JPA aggregates computing real interview rates, offer rates, average match scores, and missing skill frequency.
  - `com.jobintel.assistant`: Context-grounded AI Career Assistant with strict anti-hallucination refusal guardrails.
- **Frontend SPA**: React 18 + TypeScript + Vite, modern CSS custom properties SaaS theme, React Router DOM v6.
- **Database**: PostgreSQL (production / Docker) + H2 (standalone local dev & integration tests).
- **DevOps**: Docker multi-stage build, `docker-compose` (App + Postgres), GitHub Actions CI, Render deployment manifest.

---

## Core Features & Workflow

1. **Authentication & Authorization**: JWT token issued on login/registration, stored in client context & attached via interceptors.
2. **Resume Upload & PDF Parsing**: Apache PDFBox parses PDF text and extracts technical keywords in real-time.
3. **Job Posting Analyzer**: Paste target job descriptions with company & role metadata.
4. **Grounded AI Resume Matching**: Evaluates resume vs. job requirements, generating a 0-100 score WITH a written explanation grounded in actual text, strong skills, missing skills, and weak areas.
5. **Skill-Truthful Resume Improvements**: Refines existing bullet points without inventing unstated credentials or fake achievements.
6. **Application Tracker**: Kanban pipeline tracking application states and objective rejection sources.
7. **Real-Data Analytics**: Calculates interview rate, offer rate, top missing skills, and role conversion stats directly from stored records.
8. **AI Career Assistant**: Answers candidate questions by querying stored data first, refusing to hallucinate non-existent patterns.

---

## Quick Start & Verification

### Prerequisites
- JDK 21
- Node.js 20+
- Maven 3.9+

### Running Backend Tests
```bash
cd backend
mvn clean test
```

### Building Frontend
```bash
cd frontend
npm install
npm run build
```

### Running with Docker Compose
```bash
docker-compose up --build
```
Access the application at `http://localhost:8080`.
