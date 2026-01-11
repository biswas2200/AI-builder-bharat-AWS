# DevDecision - Tech Stack Comparison Tool

A modern web application that helps developers make informed decisions by comparing technologies based on multiple criteria and constraints. Built with a modular monolithic architecture and featuring AI-powered insights.

## Architecture

- **Backend**: Java 21 + Spring Boot 3.2+ with Spring Modulith
- **Frontend**: React 18+ with TypeScript and Vite
- **Database**: PostgreSQL for persistent storage
- **Cache**: Redis for performance optimization
- **AI**: Google Gemini 1.5 Flash for insights and recommendations
- **Styling**: TailwindCSS with "Neon & Void" design system

## Project Structure

```
devdecision/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/devdecision/
│   │   ├── inventory/       # Technology data management
│   │   ├── referee/         # Scoring and comparison logic
│   │   ├── insight/         # AI analysis and visualization
│   │   └── shared/          # Cross-cutting concerns
│   └── src/test/java/       # Backend tests (JUnit 5 + QuickCheck)
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── contexts/        # React contexts (Theme, etc.)
│   │   ├── pages/           # Page components
│   │   └── __tests__/       # Frontend tests (Jest + RTL)
└── docker-compose.yml       # Local development services
```

## Getting Started

### Prerequisites

- Java 21+
- Node.js 18+
- Docker and Docker Compose
- Maven 3.8+
- Google Cloud Project with Vertex AI API enabled

### Configuration

1. **Google Cloud Setup:**
   - Create a Google Cloud project
   - Enable Vertex AI API
   - Create a service account and download the JSON key file
   - Place the key file in the root directory (it's already gitignored)

2. **Environment Configuration:**
   ```bash
   # Copy the environment template
   cp .env.example .env
   
   # Edit .env with your actual values
   # The Google Cloud credentials file is already configured to point to:
   # ai-refree-aws-814936425b85.json (in root directory)
   ```

### Local Development Setup

1. **Start infrastructure services:**
   ```bash
   docker-compose up -d
   ```

2. **Backend setup:**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

3. **Frontend setup:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Access the application:**
   - Frontend: http://localhost:3000 (or next available port)
   - Backend API: http://localhost:8080

### Standalone Development (No Docker Required)

For development without Docker dependencies:

1. **Backend setup:**
   ```bash
   cd backend
   $env:SPRING_PROFILES_ACTIVE='standalone'; mvn spring-boot:run
   ```

2. **Frontend setup:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. **Access the application:**
   - Frontend: http://localhost:3000 (or next available port)
   - Backend API: http://localhost:8080
   - H2 Database Console: http://localhost:8080/h2-console

### Running Tests

**Backend tests:**
```bash
cd backend
mvn test
```

**Frontend tests:**
```bash
cd frontend
npm test
```

## Features

- 🔍 **Technology Search & Discovery**: Browse and search technologies by category, name, and tags
- ⚖️ **Weighted Scoring Algorithm**: Compare technologies based on customizable criteria
- 📊 **Interactive Visualizations**: Radar charts, KPI cards, and trend analysis
- 🤖 **AI-Powered Insights**: Personalized recommendations using Google Gemini
- 🌓 **Dark/Light Mode**: Modern "Neon & Void" design system
- 🏗️ **Modular Architecture**: Domain-driven design with Spring Modulith
- ⚡ **Performance Optimized**: Redis caching and efficient data structures

## Design System: "Neon & Void"

- **Light Mode**: Soft purple gradients with clean white backgrounds
- **Dark Mode**: Deep void backgrounds with glowing purple accents
- **Interactive Effects**: Glassmorphism, hover animations, and neon glows
- **Responsive Design**: Mobile-first approach with Tailwind CSS

## Testing Strategy

- **Property-Based Testing**: QuickCheck for Java validates universal properties
- **Unit Testing**: JUnit 5 for backend, Jest + RTL for frontend
- **Integration Testing**: Spring Boot Test with Testcontainers
- **End-to-End Coverage**: Both specific examples and comprehensive input validation

## Contributing

This project follows the spec-driven development methodology. See the `.kiro/specs/dev-decision/` directory for detailed requirements, design, and implementation tasks.

## License

MIT License - see LICENSE file for details