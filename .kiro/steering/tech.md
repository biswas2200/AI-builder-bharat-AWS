# Technology Stack & Build System

## Backend Stack

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.2+** - Main application framework
- **Spring Modulith** - Modular monolithic architecture with domain boundaries
- **PostgreSQL** - Primary database for persistent storage
- **Redis** - Caching layer for performance optimization
- **Google Cloud Vertex AI** - Gemini 1.5 Flash integration for AI insights
- **Maven** - Build and dependency management

### Key Dependencies
- Spring Boot Starters: Web, Data JPA, Data Redis, Validation, Cache
- Testing: JUnit 5, QuickCheck (property-based testing), Testcontainers
- Database: PostgreSQL driver, H2 (test only)

## Frontend Stack

- **React 18+** - UI framework with modern hooks and concurrent features
- **TypeScript** - Type safety and enhanced developer experience
- **Vite** - Fast build tool and development server
- **TailwindCSS** - Utility-first CSS framework with custom "Neon & Void" theme
- **Framer Motion** - Animation library for smooth interactions
- **Recharts** - Data visualization components
- **React Router** - Client-side routing

### Key Dependencies
- UI: Lucide React (icons), Framer Motion (animations)
- HTTP: Axios for API communication
- Testing: Jest, React Testing Library, @testing-library/user-event

## Development Environment

### Prerequisites
- Java 21+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose

### Infrastructure Services
```bash
# Start PostgreSQL and Redis
docker-compose up -d
```

## Common Commands

### Backend Development
```bash
cd backend

# Clean build and install dependencies
mvn clean install

# Run application (development mode)
mvn spring-boot:run

# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Package for production
mvn clean package
```

### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Start development server (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Run tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Lint code
npm run lint

# Preview production build
npm run preview
```

### Full Stack Development
```bash
# Terminal 1: Start infrastructure
docker-compose up -d

# Terminal 2: Start backend
cd backend && mvn spring-boot:run

# Terminal 3: Start frontend
cd frontend && npm run dev
```

## Build Configuration

### Backend Configuration
- **Application Properties**: `application.yml` with environment-specific overrides
- **Maven**: Standard Spring Boot parent with custom dependencies
- **Profiles**: Default, test profiles configured

### Frontend Configuration
- **Vite**: Configured with React plugin and API proxy to backend
- **TypeScript**: Strict mode enabled with modern target
- **TailwindCSS**: Custom theme extending default with purple color palette
- **ESLint**: TypeScript-aware linting with React-specific rules

## Testing Strategy

### Backend Testing
- **Unit Tests**: JUnit 5 with Spring Boot Test
- **Property-Based Testing**: QuickCheck for comprehensive input validation
- **Integration Tests**: Testcontainers for database testing
- **Modulith Testing**: Spring Modulith test support for module boundaries

### Frontend Testing
- **Unit Tests**: Jest with React Testing Library
- **Component Testing**: Isolated component behavior testing
- **User Interaction Testing**: @testing-library/user-event for realistic interactions
- **Coverage**: Jest coverage reporting

## Performance & Caching

### Backend Caching
- **Redis**: Configured with connection pooling and TTL settings
- **Spring Cache**: Annotation-driven caching with Redis backend
- **Custom TTL**: Technology data (1 hour), Comparisons (30 minutes)

### Frontend Optimization
- **Vite**: Fast HMR and optimized production builds
- **Code Splitting**: Automatic route-based splitting
- **Asset Optimization**: Built-in minification and compression