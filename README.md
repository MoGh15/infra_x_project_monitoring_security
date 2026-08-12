<h1 align="center">PraxisForm</h1>

<p align="center">
  Digital patient intake for medical practices – paperless, secure, and in real time.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white" />
</p>

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Getting Started](#-installation--getting-started)
- [Environment Variables](#-environment-variables)
- [API Overview](#-api-overview)
- [Tests](#-tests)
- [Deployment](#-deployment)

---

## 🩺 About the Project

**PraxisForm** replaces the traditional paper-based medical history form in doctor's offices with a fully digital system. Patients fill out a multi-step online form – including personal data, medical history, symptom assessment, file attachments, and a digital signature. Practice staff can view incoming submissions in real time on an admin dashboard, and can edit, filter, and mark them as completed.

---

## ✨ Features

| Area | Functionality |
|------|---------------|
| **Patient Form** | Multi-step stepper with validation (Personal Data → Medical History → Symptoms → Consent → Signature) |
| **File Upload** | Up to 5 files (PDF, JPEG, PNG, WebP), max. 10 MB per file, stored via MongoDB GridFS |
| **Digital Signature** | Touch-/mouse-based signature capture directly in the browser |
| **Admin Dashboard** | Tabular overview of all submissions with filtering, search, and pagination |
| **Real-Time Updates** | Server-Sent Events (SSE) for live notifications on new submissions |
| **Status Workflow** | Three-stage workflow: `NEW` → `VIEWED` → `DONE` |
| **Detail View** | Full view & editing of individual submissions incl. file download |
| **Authentication** | JWT-based admin authentication with automatic admin seeding |
| **Responsive UI** | Angular Material Design – optimized for desktop, tablet, and mobile devices |

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Usage |
|------------|---------|-------|
| **Java** | 17+ | Programming language |
| **Spring Boot** | 4.0 | REST API framework |
| **Spring Security** | – | Authentication & authorization |
| **Spring Data MongoDB** | – | Database access & GridFS |
| **JJWT** | 0.12.5 | JWT token generation & validation |
| **Lombok** | – | Boilerplate reduction |
| **JaCoCo** | 0.8.12 | Code coverage reports |
| **Maven** | 3.9+ | Build tool |

### Frontend
| Technology | Version | Usage |
|------------|---------|-------|
| **Angular** | 21 | SPA framework |
| **Angular Material** | 21 | UI component library |
| **TypeScript** | 5.9 | Programming language |
| **RxJS** | 7.8 | Reactive programming |
| **Vitest** | 4.0 | Unit testing |
| **Nginx** | alpine | Production web server |

### Infrastructure
| Technology | Usage |
|------------|-------|
| **MongoDB** | Document database & GridFS for file uploads |
| **Docker / Docker Compose** | Containerization & orchestration |
| **GitLab CI/CD** | Automated builds & deployments |

---

## 📁 Project Structure

```
PraxisForm/
├── docker-compose.yml              # Local development environment
├── docker-compose.prod.yml         # Production configuration
│
├── praxis/                         # 🔧 Spring Boot Backend
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/.../praxis/
│   │   ├── PraxisApplication.java          # Entry point
│   │   ├── controllers/
│   │   │   ├── AuthController.java         # POST /api/auth/login
│   │   │   ├── PublicSubmissionController   # POST /api/submissions
│   │   │   ├── AdminSubmissionController    # CRUD /api/admin/submissions
│   │   │   └── AdminSubmissionsStreamCtrl   # GET  /api/admin/submissions/stream (SSE)
│   │   ├── modules/
│   │   │   ├── patient/                    # Submission, PatientData, MedicalData, ...
│   │   │   ├── admin/                      # Admin, JwtService, SecurityConfig, SseHub
│   │   │   └── form/                       # FormDefinition (JSON Schema)
│   │   ├── DTOs/                           # Request/Response objects
│   │   └── repos/                          # Spring Data Repositories
│   └── src/test/                           # Unit & integration tests
│
├── praxis-frontend/                # 🎨 Angular Frontend
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── src/app/
│   │   ├── features/
│   │   │   ├── patient-form/               # Patient form (stepper)
│   │   │   ├── admin/                      # Admin dashboard with SSE
│   │   │   ├── submission-details/         # Submission detail view
│   │   │   ├── login/                      # Admin login
│   │   │   └── submission-success/         # Success page after submission
│   │   ├── core/
│   │   │   ├── api/                        # API services & models
│   │   │   ├── auth/                       # AuthService, AuthGuard
│   │   │   └── consent/                    # Consent management
│   │   └── shared/                         # Reusable components
│   └── src/assets/
│       └── env.template.js                 # Runtime environment variables
│
└── docs/
    └── class-diagram.puml          # UML class diagram (PlantUML)
```

---

## 📦 Prerequisites

| Tool | Minimum Version |
|------|-----------------|
| **Docker** & **Docker Compose** | 20.10+ / v2 |
| **Java JDK** *(local backend development only)* | 17+ |
| **Node.js** *(local frontend development only)* | 22+ |
| **Maven** *(optional, wrapper included)* | 3.9+ |

---

## 🚀 Installation & Getting Started

### Option 1: Full-Stack with Docker (recommended)

```bash
# Clone the repository
git clone <repository-url>
cd PraxisForm

# Start all services
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | [http://localhost:4200](http://localhost:4200) |
| Backend API | [http://localhost:8080](http://localhost:8080) |
| MongoDB | `localhost:27017` |

### Option 2: Local Development

**Start the backend:**

```bash
cd praxis

# MongoDB must be running locally or via Docker:
docker compose up mongodb -d

# Start Spring Boot (Windows)
mvnw.cmd spring-boot:run

# Start Spring Boot (Linux/macOS)
./mvnw spring-boot:run
```

**Start the frontend:**

```bash
cd praxis-frontend
npm install
npm start
```

The frontend is available at [http://localhost:4200](http://localhost:4200).

---

## ⚙️ Environment Variables

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/praxis` |
| `JWT_SECRET` | Secret key for JWT signing (min. 32 characters) | `CHANGE_ME_very_long_secret_at_least_32_chars` |
| `JWT_EXP_MINUTES` | JWT token validity duration in minutes | `120` |
| `ADMIN_DEFAULT_PASSWORD` | Initial admin password (on first startup) | `changeme` |
| `API_URL` | Backend API URL for the frontend | `https://dev.praxis-form.de` |

> ⚠️ **Important:** Make sure to change `JWT_SECRET` and `ADMIN_DEFAULT_PASSWORD` in production environments!

---

## 🔌 API Overview

### Public (no token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/submissions` | Submit a new patient form |
| `POST` | `/api/auth/login` | Admin login, returns JWT |

### Admin (JWT token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/submissions` | Retrieve all submissions (optional `?status=NEW`) |
| `GET` | `/api/admin/submissions/:id` | Retrieve a single submission |
| `PATCH` | `/api/admin/submissions/:id` | Edit a submission |
| `PATCH` | `/api/admin/submissions/:id/status` | Update status (`NEW` → `VIEWED` → `DONE`) |
| `GET` | `/api/admin/submissions/:id/attachments/:fileId` | Download a file attachment |
| `GET` | `/api/admin/submissions/stream` | SSE stream for real-time updates |

---

## 🧪 Tests

### Backend Tests

```bash
cd praxis

# Run all tests
mvnw.cmd test          # Windows
./mvnw test            # Linux/macOS

# Generate coverage report (JaCoCo)
# Report available at: target/site/jacoco/index.html
```

---

## 🌐 Deployment

### Production with Docker Compose

```bash
# Set environment variables
export CI_REGISTRY_IMAGE=registry.gitlab.com/infra-x-group/<your-project>
export JWT_SECRET="a_secure_secret_key_at_least_32_characters"
export ADMIN_DEFAULT_PASSWORD="secure_admin_password"

# Start production compose
docker compose -f docker-compose.prod.yml up -d
```

### GitLab CI/CD

The project is designed for use with **GitLab Container Registry**. The production `docker-compose.prod.yml` pulls pre-built images from the registry:

```yaml
image: ${CI_REGISTRY_IMAGE}/backend:latest
image: ${CI_REGISTRY_IMAGE}/frontend:latest
```

### Recommended Production Stack

```
Client → Nginx Reverse Proxy (SSL) → Frontend (:80) → Backend (:8080) → MongoDB (:27017)
```

---

<p align="center">
  Built with ❤️ for the digital medical practice.
</p>

