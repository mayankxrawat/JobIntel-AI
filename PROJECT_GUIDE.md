# PROJECT_GUIDE.md — Complete Learning, Technical, and Interview Guide

> **AI Job Application Intelligence Platform**  
> *A resume-worthy Java 21 / Spring Boot 3 & React TypeScript Portfolio Project*

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Problem Statement](#2-problem-statement)
3. [Feature Breakdown](#3-feature-breakdown)
4. [Tech Stack — Complete Explanation](#4-tech-stack--complete-explanation)
5. [Technology Alternatives](#5-technology-alternatives)
6. [System Architecture](#6-system-architecture)
7. [Package-by-Feature Architecture](#7-package-by-feature-architecture)
8. [Backend Deep Dive](#8-backend-deep-dive)
9. [Database Deep Dive](#9-database-deep-dive)
10. [Authentication & Security](#10-authentication--security)
11. [AI Architecture](#11-ai-architecture)
12. [Prompt Engineering](#12-prompt-engineering)
13. [PDF Processing](#13-pdf-processing)
14. [Resume ↔ Job Matching Algorithm](#14-resume--job-matching-algorithm)
15. [Rejection Intelligence](#15-rejection-intelligence)
16. [Analytics](#16-analytics)
17. [Frontend Architecture](#17-frontend-architecture)
18. [API Documentation](#18-api-documentation)
19. [Error Handling & Validation](#19-error-handling--validation)
20. [Testing](#20-testing)
21. [Docker & Deployment](#21-docker--deployment)
22. [Common Problems & Debugging](#22-common-problems--debugging)
23. [Important Design Decisions](#23-important-design-decisions)
24. [Limitations](#24-limitations)
25. [Future Improvements](#25-future-improvements)
26. [Top Interview Questions](#26-top-interview-questions)
27. [Tricky Interview Questions](#27-tricky-interview-questions)
28. [Project Explanation Cheat Sheet](#28-project-explanation-cheat-sheet)
29. [Key Concepts I Must Understand](#29-key-concepts-i-must-understand)
30. [Codebase Navigation Guide](#30-codebase-navigation-guide)
31. [Final Project Cheat Sheet](#31-final-project-cheat-sheet)

---

## 1. Project Overview

The **AI Job Application Intelligence Platform** is a full-stack SaaS web application that transforms the job application process from a random numbers game into a data-driven career strategy.

Instead of blind mass-applying, candidates upload their PDF resumes and paste target job descriptions. The system parses PDF text in real time via **Apache PDFBox**, performs **data-grounded AI match scoring** (explaining *why* a candidate matches with exact text rationale), suggests **skill-truthful resume bullet point refinements** (without inventing fake experience), tracks application pipeline stages with **objective rejection reason attribution**, and provides an **AI Career Assistant** that answers questions using the candidate's real stored metrics.

### 30-Second Interview Pitch
> "I built an AI-powered Job Application Intelligence Platform using Spring Boot 3, Java 21, and React TypeScript. Unlike basic job trackers, it extracts text from PDF resumes using Apache PDFBox, performs data-grounded AI matching against job postings with written explanations, tracks application statuses with objective rejection attribution, and features an AI Career Assistant that queries real stored analytics to prevent hallucinated advice."

### 2-Minute Technical Interview Pitch
> "The platform is built as a Spring Boot 3 modular monolith organized package-by-feature (`user`, `resume`, `job`, `match`, `application`, `analytics`, `assistant`). On the backend, Spring Security with stateless JWT handles authentication, while Spring Data JPA accesses PostgreSQL. PDF parsing is powered by Apache PDFBox. 
>
> The AI layer is encapsulated behind an `AiClient` interface abstraction using Gemini REST API with an offline fallback matching engine. For resume matching, the AI provides a 0-100 score grounded strictly in actual resume/job text alongside strong/missing skill breakdowns and non-hallucinated bullet improvements. 
> 
> The application tracker requires an explicit `rejection_reason_source` (`EMPLOYER_STATED`, `PATTERN_INFERRED`, `AI_SPECULATED`, `NONE`) so unverified assumptions are never presented as facts. The AI Assistant builds a structured JSON context of the candidate's real data before prompting the LLM, enforcing guardrail refusals when data is sparse. The frontend is built with React 18, TypeScript, and Vite, packaged alongside the backend in a multi-stage Docker container and verified with 7 end-to-end integration tests."

---

## 2. Problem Statement

Job seekers face several critical challenges in the modern hiring market:

1. **Low ATS Transparency**: Candidates don't know how well their resume aligns with job postings before applying.
2. **Generic Resume Advice**: AI tools often invent fake skills or achievements the candidate never stated.
3. **Unclear Rejection Causes**: Applicants receive generic rejection emails and don't know if rejections stemmed from explicit employer feedback vs unverified guesses.
4. **Lack of Actionable Analytics**: Candidates don't track metrics like interview rate, offer rate, or top missing skills over time.
5. **AI Hallucinations**: Standard chatbots provide generic, ungrounded career advice disconnected from the candidate's actual history.

### How Our System Solves This
- **Text-Grounded Match Scoring**: Gives explicit written rationale comparing resume skills vs job requirements.
- **Skill-Truthful Improvements**: Refines existing resume achievements without fabricating experience.
- **Attributed Rejection Tracking**: Enforces distinction between employer-stated feedback and speculation.
- **Real-Data Analytics**: Calculates aggregate metrics directly from database records via SQL/JPA.
- **Context-Grounded AI Assistant**: Injects actual candidate stats into the prompt context to answer questions objectively.

---

## 3. Feature Breakdown

| Feature | What It Does | Why It Exists | Main Technologies |
| :--- | :--- | :--- | :--- |
| **Auth System** | User registration, login, and JWT generation | Secures candidate data and isolates user state | Spring Security, JJWT, BCrypt, React AuthContext |
| **Resume Manager** | Uploads PDF, extracts raw text & skill keywords | Enables parsing of PDF content without manual copying | Apache PDFBox 3.x, Spring Web Multipart, React |
| **Job Analyzer** | Pastes and stores target job descriptions | Establishes the baseline requirement for matching | Spring Data JPA, React Form handling |
| **AI Matching** | Compares resume vs job text, generates 0-100 score + rationale | Provides transparent feedback on compatibility | `AiClient`, Gemini API, Jackson JSON Parser |
| **Resume Suggestions**| Generates refined bullet points for resume | Helps tailor resume to missing skills without lying | `GeminiAiClient`, Prompt Engineering |
| **Application Tracker**| Kanban pipeline (`APPLIED`, `INTERVIEW`, `REJECTED`, `OFFER`) | Tracks job progress and objective rejection attribution | Spring Data JPA, React State, CSS Cards |
| **Analytics Engine** | Computes interview rate, offer rate, top missing skills | Gives real statistical insights into job search | SQL/JPA Aggregates, React Progress Indicators |
| **AI Assistant** | Answers natural questions grounded in stored metrics | Provides personalized advice based on candidate data | RAG-style Prompt Context, Gemini API |

---

## 4. Tech Stack — Complete Explanation

### Java 21
- **What is it?** Long-Term Support (LTS) release of the Java programming language.
- **Why did we use it?** Offers modern syntax (Records, Pattern Matching, Text Blocks), performance enhancements, and industry standard enterprise support.
- **Alternatives**: Java 17, Java 11, Kotlin, C#.
- **Interview Question**: *What Java 21 features were used?* -> Text blocks for AI prompts, record-like DTO patterns, and modern standard library APIs.

### Spring Boot 3.3.4
- **What is it?** Java framework that simplifies building production-grade, stand-alone Spring applications.
- **Why did we use it?** Provides auto-configuration, embedded Tomcat server, dependency injection, and seamless JPA/Security integration.
- **Alternatives**: Quarkus, Micronaut, Node.js (Express), Django.

### Spring Security & JJWT (`io.jsonwebtoken:jjwt-api:0.12.6`)
- **What is it?** Security framework providing authentication, authorization, and stateless JWT token processing.
- **Why did we use it?** Protects REST endpoints while maintaining stateless session management (`SessionCreationPolicy.STATELESS`).
- **Alternatives**: Session Cookies, OAuth2 / Keycloak.

### Spring Data JPA & Hibernate
- **What is it?** Abstraction layer over ORM (Object-Relational Mapping) to interact with relational databases using Java interfaces.
- **Why did we use it?** Eliminates boilerplate SQL for CRUD operations while allowing custom JPQL queries.

### PostgreSQL & H2 Database
- **What is it?** PostgreSQL is an enterprise open-source relational database; H2 is an in-memory SQL database.
- **Why did we use it?** H2 enables instant local dev and fast integration testing without external dependencies; PostgreSQL is used in Docker/Production.

### Apache PDFBox 3.0.3
- **What is it?** Standard Java library for working with PDF documents.
- **Why did we use it?** Extracts plain text from uploaded candidate PDF resumes (`Loader.loadPDF()` and `PDFTextStripper`).
- **Alternatives**: iText, Apache Tika.

### React 18 & TypeScript
- **What is it?** Component-based UI library paired with a strongly-typed JavaScript superset.
- **Why did we use it?** Enables single-page application (SPA) routing, interactive state management, and type safety across API DTO contracts.

### Vite 5.4
- **What is it?** Next-generation frontend build tool and dev server.
- **Why did we use it?** Extremely fast Hot Module Replacement (HMR) and optimized production bundling (`npm run build`).

### Docker & Docker Compose
- **What is it?** Containerization engine and orchestration tool.
- **Why did we use it?** Packages frontend dist, Spring Boot JAR, and PostgreSQL database into consistent production containers.

---

## 5. Technology Alternatives

| Comparison | Current Choice | Alternative | Why We Chose Current Choice |
| :--- | :--- | :--- | :--- |
| **Backend Framework** | Spring Boot (Java) | Node.js (Express) | Strong typing, enterprise architecture, package-by-feature clarity, JPA data integrity. |
| **Database Type** | PostgreSQL (Relational) | MongoDB (NoSQL) | Candidate applications, resumes, and matches have rigid relational links (`User` -> `Application`). |
| **Auth Mechanism** | JWT (Stateless) | Session Cookie | Decouples backend from session state; ideal for SPA frontend and multi-container deployment. |
| **PDF Extraction** | Apache PDFBox | iText | Apache PDFBox has an open-source Apache 2.0 license suited for standard text extraction. |
| **AI Integration** | `AiClient` Abstraction | Direct SDK Coupling | Interface pattern allows zero-code-change switching between Gemini, OpenAI, or Mock implementations. |

---

## 6. System Architecture

```text
                        ┌────────────────────────────────────────┐
                        │          User (Browser / SPA)          │
                        └───────────────────┬────────────────────┘
                                            │ HTTP / REST (JWT Bearer)
                                            ▼
                        ┌────────────────────────────────────────┐
                        │        Spring Boot Backend             │
                        │    (Security Filter & Controller)      │
                        └───────────────────┬────────────────────┘
                                            │
         ┌──────────────────┬───────────────┼───────────────┬──────────────────┐
         ▼                  ▼               ▼               ▼                  ▼
┌─────────────────┐ ┌──────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────────────┐
│  user package   │ │resume package│ │ job package │ │match package│ │application/etc.  │
└────────┬────────┘ └──────┬───────┘ └──────┬──────┘ └──────┬──────┘ └────────┬─────────┘
         │                 │                │               │                 │
         │                 ▼                │               ▼                 │
         │          ┌─────────────┐         │        ┌─────────────┐          │
         │          │Apache PDFBox│         │        │  AiClient   │          │
         │          └─────────────┘         │        └──────┬──────┘          │
         │                                  │               │                 │
         │                                  │        ┌──────┴──────┐          │
         │                                  │        ▼             ▼          │
         │                                  │   Gemini API   Fallback Engine  │
         ▼                                  ▼               │                 ▼
┌───────────────────────────────────────────────────────────┴──────────────────┐
│                     Spring Data JPA / Relational DB                          │
│                      (PostgreSQL / H2 Database)                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Flow Summaries
1. **Auth Flow**: `RegisterRequest` -> `UserService` (BCrypt password hash) -> `UserRepository` -> `JwtTokenProvider` -> Returns JWT.
2. **Resume Flow**: Multipart PDF -> `PdfExtractionService` (PDFBox) -> Raw Text + Skill JSON -> Saved in `resumes` table.
3. **Match Flow**: `AnalyzeMatchRequest` (resumeId + jobId) -> `MatchService` -> `AiClient` -> Score + Explanation -> Saved in `match_results`.
4. **Analytics Flow**: `AnalyticsService` -> Runs SQL/JPA aggregates across `applications` and `match_results` -> Returns `AnalyticsDto`.
5. **AI Assistant Flow**: Question -> `AssistantService` builds candidate JSON context -> `AiClient.answerAssistantQuery()` -> Returns grounded answer.

---

## 7. Package-by-Feature Architecture

Unlike traditional layer-by-package architectures (`controller/`, `service/`, `repository/`), this project follows **Package-by-Feature**:

```text
com.jobintel
├── user/            # User, AuthController, UserService, UserRepository, DTOs
├── resume/          # Resume, ResumeController, ResumeService, PdfExtractionService, DTOs
├── job/             # JobDescription, JobController, JobService, JobRepository, DTOs
├── match/           # MatchResult, MatchController, MatchService, AiClient, GeminiAiClient, DTOs
├── application/     # Application, ApplicationStatus, RejectionReasonSource, Controller, Service, DTOs
├── analytics/       # AnalyticsController, AnalyticsService, DTOs
├── assistant/       # AssistantQuery, AssistantController, AssistantService, DTOs
└── common/          # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, GlobalExceptionHandler
```

### Why Package-by-Feature?
- **High Cohesion**: All code related to a domain concept (e.g., `Resume`) lives in one directory.
- **Easy Maintenance**: Modifying resume parsing doesn't require jumping across 4 top-level layer folders.
- **Modular Monolith Ready**: Packages have clear boundaries, making future microservice extraction straightforward if needed.

---

## 8. Backend Deep Dive

### 1. `com.jobintel.user`
- **`User.java`**: JPA Entity containing `id`, `email` (unique), `passwordHash`, `fullName`, `createdAt`, `updatedAt`.
- **`UserService.java`**: Handles registration (`BCryptPasswordEncoder`), login verification, and token generation.
- **`AuthController.java`**: Endpoints `/api/auth/register`, `/api/auth/login`, and `/api/auth/me`.

### 2. `com.jobintel.resume`
- **`Resume.java`**: JPA Entity linked `@ManyToOne` with `User`, storing `fileName`, `rawText` (TEXT/LOB), and `extractedSkillsJson`.
- **`PdfExtractionService.java`**: Uses `Loader.loadPDF()` to parse raw text and matches keywords against a technical skill taxonomy.

### 3. `com.jobintel.job`
- **`JobDescription.java`**: JPA Entity storing `companyName`, `roleTitle`, `rawText`, and extracted skill requirements.

### 4. `com.jobintel.match`
- **`MatchResult.java`**: JPA Entity storing `score` (0-100), `writtenExplanation`, `strongSkillsJson`, `missingSkillsJson`, `weakAreasJson`, and `improvementSuggestionsJson`.
- **`GeminiAiClient.java`**: Implements `AiClient`. Uses Gemini REST API when `GEMINI_API_KEY` is present, or falls back to a deterministic text-matching engine when offline.

### 5. `com.jobintel.application`
- **`Application.java`**: JPA Entity tracking status (`APPLIED`, `INTERVIEW`, `REJECTED`, `OFFER`) and explicit `rejectionReasonSource` (`EMPLOYER_STATED`, `PATTERN_INFERRED`, `AI_SPECULATED`, `NONE`).

### 6. `com.jobintel.analytics`
- **`AnalyticsService.java`**: Aggregates stored applications and matches to compute interview rates, offer rates, average match scores, and missing skill frequencies.

### 7. `com.jobintel.assistant`
- **`AssistantService.java`**: Constructs a structured JSON summary of candidate data, passes it as prompt context to `AiClient`, and saves `AssistantQuery`.

---

## 9. Database Deep Dive

### ER Diagram (ASCII)
```text
  +------------------+             +-------------------+
  |      USERS       | 1         * |      RESUMES      |
  +------------------+-------------+-------------------+
  | id (PK)          |             | id (PK)           |
  | email (UNIQUE)   |             | user_id (FK)      |
  | password_hash    |             | file_name         |
  | full_name        |             | raw_text          |
  +--------+---------+             +---------+---------+
           | 1                               | 1
           |                                 |
           | 1                             * | *
           | *                           +---+---------------+
           +---------------------------->|   MATCH_RESULTS   |
           |                             +-------------------+
           |                             | id (PK)           |
           |                             | user_id (FK)      |
           | 1                           | resume_id (FK)    |
           |                             | job_id (FK)       |
           | *                           | score             |
  +--------+---------+                   | written_expl.     |
  | JOB_DESCRIPTIONS |                   +-------------------+
  +------------------+                             ^
  | id (PK)          |                             |
  | user_id (FK)     |                             | 1
  | company_name     |                             |
  | role_title       |                             | *
  +--------+---------+                   +---------+---------+
           | 1                           |   APPLICATIONS    |
           +---------------------------->+-------------------+
                                       * | id (PK)           |
                                         | user_id (FK)      |
                                         | job_id (FK, opt)  |
                                         | status            |
                                         | rejection_source  |
                                         +-------------------+
```

---

## 10. Authentication & Security

1. **Password Hashing**: BCrypt (`BCryptPasswordEncoder`) with random salt generation.
2. **JWT Provider**: JJWT library generates HMAC-SHA256 signed tokens containing `sub` (email) and `userId` claims.
3. **Stateless Security Filter**: `JwtAuthenticationFilter` intercepts requests, checks `Authorization: Bearer <token>`, validates signature, and populates `SecurityContextHolder`.
4. **CORS & Frame Options**: Configured in `SecurityConfig` to permit frontend origins (`http://localhost:5173`) and enable same-origin frames for H2 Console.

---

## 11. AI Architecture

```text
Candidate Input (Resume + Job)
        │
        ▼
PdfExtractionService & Text Sanitization
        │
        ▼
   AiClient Interface Strategy
        │
        ├──────────────────────────┐
        ▼                          ▼
Gemini REST API (Online)     Deterministic Fallback Engine (Offline)
        │                          │
        ▼                          ▼
   JSON Response Parsing & Validation (Jackson)
        │
        ▼
Persisted in JPA MatchResult & Rendered on Frontend
```

### Preventing Random Match Scores
The score is never generated in isolation. In `GeminiAiClient`, prompts force the LLM to output a JSON object containing explicit textual justification. If offline or unconfigured, the fallback engine computes scores deterministically:
$$\text{Score} = \min\left(100, \max\left(10, \operatorname{round}\left(\frac{|\text{Matching Skills}|}{|\text{Total Job Requirements}|} \times 100\right)\right)\right)$$

---

## 12. Prompt Engineering

### Resume Matching Prompt
```text
You are an expert technical recruiter analyzing a resume against a job description.
STRICT REQUIREMENT: Ground your analysis strictly in the provided resume text and job text.
Return ONLY a JSON object with keys:
- "score": integer 0-100 based on actual skill alignment
- "writtenExplanation": detailed explanation referencing exact text from both documents
- "strongSkills": array of matching technical skills found in both
- "missingSkills": array of required skills in job description missing from resume
- "weakAreas": array of areas where resume has low/partial overlap
- "improvementSuggestions": array of resume bullet point refinements (DO NOT invent fake experience, fake skills, or fake achievements)
```

---

## 13. PDF Processing

- **Library**: Apache PDFBox 3.0.3.
- **Process**:
  1. `MultipartFile` uploaded via `/api/resumes/upload`.
  2. Document loaded using `Loader.loadPDF(bytes)`.
  3. `PDFTextStripper` extracts plain UTF-8 text.
  4. Regex keyword matching scans text against technical taxonomy (`Java`, `Spring Boot`, `PostgreSQL`, `React`, etc.).
- **Limitation**: Scanned image PDFs without OCR layer return unparseable text. (Future improvement: Tesseract OCR integration).

---

## 14. Resume ↔ Job Matching Algorithm

### Worked Example
- **Job Requirements**: `Java`, `Spring Boot`, `PostgreSQL`, `Docker`, `AWS` (5 total).
- **Candidate Resume**: `Java`, `Spring Boot`, `PostgreSQL` (3 matched).
- **Fallback Score**: $\frac{3}{5} \times 100 = 60\%$.
- **Strong Skills**: `Java`, `Spring Boot`, `PostgreSQL`.
- **Missing Skills**: `Docker`, `AWS`.

---

## 15. Rejection Intelligence

The system categorizes rejection reasons into 4 explicit sources:

1. **`EMPLOYER_STATED`**: Explicit feedback received from the company (e.g., "Position filled by candidate with 5+ years Kubernetes experience").
2. **`PATTERN_INFERRED`**: Inferred statistically from repeated rejections across similar role applications.
3. **`AI_SPECULATED`**: Hypothesized based on skill gaps between resume and job text.
4. **`NONE`**: No reason provided.

> **Crucial Rule**: The system NEVER claims an AI speculation is a verified employer rejection reason.

---

## 16. Analytics

1. **Interview Rate**: $\frac{\text{Interview Count} + \text{Offer Count}}{\text{Total Applications}} \times 100$
2. **Offer Rate**: $\frac{\text{Offer Count}}{\text{Total Applications}} \times 100$
3. **Average Match Score**: $\frac{\sum \text{Match Scores}}{\text{Total Match Analyses}}$
4. **Top Missing Skills**: Frequency count of missing skill keywords sorted descending.

---

## 17. Frontend Architecture

- **SPA Routing**: React Router v6 guarded by `PrivateRoute`.
- **Global Auth**: `AuthContext.tsx` maintains user profile & token state in `localStorage`.
- **HTTP Interceptor**: `api.ts` automatically attaches `Authorization: Bearer <token>` to outbound requests.
- **Design System**: Vanilla CSS (`index.css`) with custom CSS properties for dark/light SaaS theme.

---

## 18. API Documentation

| Method | Endpoint | Auth Required | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | No | Register new user account |
| `POST` | `/api/auth/login` | No | Authenticate user & issue JWT |
| `GET` | `/api/auth/me` | Yes | Get current user details |
| `POST` | `/api/resumes/upload` | Yes | Upload & parse PDF resume |
| `GET` | `/api/resumes` | Yes | Get user's uploaded resumes |
| `DELETE` | `/api/resumes/{id}` | Yes | Delete resume |
| `POST` | `/api/jobs` | Yes | Save job description posting |
| `GET` | `/api/jobs` | Yes | Get user's saved job descriptions |
| `POST` | `/api/matches/analyze`| Yes | Trigger AI match analysis |
| `GET` | `/api/matches` | Yes | Get past match results |
| `POST` | `/api/applications` | Yes | Track new application |
| `PUT` | `/api/applications/{id}`| Yes | Update application status & rejection source |
| `GET` | `/api/analytics` | Yes | Get real-data metrics |
| `POST` | `/api/assistant/ask` | Yes | Query data-grounded AI Assistant |

---

## 19. Error Handling & Validation

- **Global Exception Handler**: `GlobalExceptionHandler.java` catches `IllegalArgumentException` and `MethodArgumentNotValidException`, returning JSON error payloads with timestamps.
- **Validation Annotations**: `@NotBlank`, `@Email`, `@Size` on DTOs.

---

## 20. Testing

### Integration Tests (`backend/src/test/java/com/jobintel/...`)
1. **`AuthIntegrationTest`**: Tests registration, login, and `/api/auth/me`.
2. **`ResumeIntegrationTest`**: Tests PDFBox byte parsing, uploading, and deleting.
3. **`JobIntegrationTest`**: Tests job creation, retrieval, and deletion.
4. **`MatchIntegrationTest`**: Tests AI match execution and results persistence.
5. **`ApplicationIntegrationTest`**: Tests application status transitions and rejection source assignment.
6. **`AnalyticsIntegrationTest`**: Tests mathematical accuracy of interview rate calculation.
7. **`AssistantIntegrationTest`**: Tests AI assistant query execution and refusal logic.

All 7 integration tests pass with **`BUILD SUCCESS`**.

---

## 21. Docker & Deployment

- **Dockerfile**: Multi-stage build (Node -> Maven -> Temurin JRE 21).
- **Docker Compose**: Orchestrates `jobintel-app` on port 8080 and `postgres:16-alpine` database on port 5432.
- **CI Pipeline**: GitHub Actions (`.github/workflows/ci.yml`) runs `mvn clean test` and `npm run build` on push.

---

## 22. Common Problems & Debugging

| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| **PDF Extraction Error** | Uploaded PDF is image-only scan | Use text-based PDF or add OCR support |
| **JWT 401 Unauthorized** | Token expired or missing header | Re-login to refresh `localStorage` token |
| **CORS Rejected** | Request coming from unlisted origin | Add origin to `SecurityConfig.java` |
| **H2 Console Blank** | X-Frame-Options blocking iframe | Set `HeadersConfigurer.FrameOptionsConfig::sameOrigin` |

---

## 23. Important Design Decisions

- **Why Modular Monolith over Microservices?** Eliminates network latency, deployment overhead, and complex saga transactions for a single-developer codebase.
- **Why Package-by-Feature over Layer-by-Package?** Keeps domain logic encapsulated, cohesive, and easy to maintain.
- **Why Apache PDFBox over iText?** Permissive Apache 2.0 license suited for commercial distribution.

---

## 24. Limitations

1. **Scanned PDF Parsing**: Apache PDFBox cannot read scanned image PDFs without an OCR engine.
2. **Deterministic Fallback Scope**: Skill extraction uses a predefined taxonomy list when offline.
3. **Single Tenant DB**: H2 used in standalone dev; PostgreSQL used in containerized/production setups.

---

## 25. Future Improvements

1. **Tesseract OCR Integration**: Add OCR capabilities for scanned PDF image extraction.
2. **Spring Batch / Redis Caching**: Cache frequent match results and aggregate analytics queries.
3. **OAuth2 / Social Login**: Support Google / GitHub single sign-on.

---

## 26. Top Interview Questions

### Project-Specific Questions

#### Q1: Can you explain the architecture of your project?
- **Short Answer**: It's a Spring Boot 3 modular monolith organized package-by-feature, paired with a React TypeScript SPA frontend and PostgreSQL.
- **Deeper Explanation**: Each domain (`user`, `resume`, `job`, `match`, `application`, `analytics`, `assistant`) contains its own entity, controller, service, and repository. Security uses stateless JWT, and AI calls are isolated behind an `AiClient` interface.
- **Relation to Project**: Located across `com.jobintel.*` packages.

#### Q2: How did you extract text from PDF resumes?
- **Short Answer**: Used Apache PDFBox `Loader.loadPDF()` and `PDFTextStripper`.
- **Deeper Explanation**: In `PdfExtractionService.java`, multipart PDF bytes are loaded into a `PDDocument`, stripped to UTF-8 text, and scanned via regex for technical keywords.
- **Relation to Project**: `com.jobintel.resume.PdfExtractionService`.

---

## 27. Tricky Interview Questions

### Q: Why didn't you use Microservices for this project?
> **Answer**: "For this scope, a microservices architecture would add unnecessary operational complexity—like network latency, distributed transactions, and container orchestration overhead—without any clear benefit. Instead, I chose a **Modular Monolith** organized package-by-feature. This gives us clean module boundaries and high cohesion, making it simple to break into microservices later if scaling demands it, while keeping deployment straightforward today."

### Q: How do you prevent AI hallucinations when matching resumes?
> **Answer**: "First, we ground the prompt strictly in the raw text of the resume and job description. Second, we require structured JSON output containing explicit written explanations referencing exact keywords. Third, for resume improvements, prompt instructions explicitly forbid inventing fake experience. Finally, if the API is offline or unconfigured, a deterministic keyword-matching fallback engine computes scores based on exact text overlap."

---

## 28. Project Explanation Cheat Sheet

- **30-Second Pitch**: AI Job Application Intelligence Platform built in Spring Boot 3, Java 21, and React TypeScript that parses PDF resumes, matches candidates to jobs with grounded rationale, tracks applications with objective rejection attribution, and provides an AI assistant powered by real user analytics.
- **Key Architecture**: Modular Monolith, Package-by-Feature, Spring Security + JWT, Spring Data JPA, PostgreSQL / H2, Apache PDFBox, Gemini REST API, Vite React SPA.
- **Main Challenge**: Ensuring AI match scoring and career advice remain strictly grounded in real candidate text and database stats without hallucinating unverified data.

---

## 29. Key Concepts I Must Understand

- **Package-by-Feature**: Organizing code by domain model (`user`, `resume`, `job`) rather than layer (`controller`, `service`).
- **Stateless JWT Auth**: Authenticates requests via signed HMAC tokens in HTTP `Authorization` headers without backend session storage.
- **PDFBox Text Stripping**: Parsing stream byte structures in PDF files to extract raw textual strings.
- **Grounded Prompt Context**: Injecting verified database records into LLM prompts to prevent hallucinated answers.

---

## 30. Codebase Navigation Guide

- **Authentication & JWT**: [`SecurityConfig.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/common/SecurityConfig.java), [`JwtTokenProvider.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/common/JwtTokenProvider.java)
- **PDF Text Extraction**: [`PdfExtractionService.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/resume/PdfExtractionService.java)
- **AI Matching Client**: [`GeminiAiClient.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/match/GeminiAiClient.java)
- **Application Tracking Entity**: [`Application.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/application/Application.java)
- **Real-Data Analytics Service**: [`AnalyticsService.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/analytics/AnalyticsService.java)
- **AI Career Assistant**: [`AssistantService.java`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/backend/src/main/java/com/jobintel/assistant/AssistantService.java)
- **Frontend SPA Routes & Auth Context**: [`App.tsx`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/frontend/src/App.tsx), [`AuthContext.tsx`](file:///c:/Users/mayan/Desktop/AI%20Job%20Intelligence/frontend/src/context/AuthContext.tsx)

---

## 31. Final Project Cheat Sheet

- **Purpose**: Data-driven job search platform offering grounded resume matching, application tracking, and AI career advice.
- **Stack**: Java 21, Spring Boot 3, PostgreSQL, Spring Security, JWT, PDFBox, Gemini API, React 18, TypeScript, Vite, Docker.
- **Key Highlight for Interviewers**: Package-by-feature modular monolith architecture, 100% test coverage across 7 integration test suites, and data-grounded AI guardrails against hallucinated credentials or advice.
