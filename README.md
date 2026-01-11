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
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080

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
     <img width="1837" height="922" alt="Screenshot 2026-01-11 221727" src="https://github.com/user-attachments/assets/75ed3310-3b8a-497d-80d6-2aff7827c274" />
- **Dark Mode**: Deep void backgrounds with glowing purple accents
  <img width="1846" height="918" alt="Screenshot 2026-01-11 202906" src="https://github.com/user-attachments/assets/c4ccf16c-9910-4903-80d2-cb6d2c48a984" />

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
