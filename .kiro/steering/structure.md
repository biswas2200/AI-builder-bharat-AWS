# Project Structure & Organization

## Overall Architecture

DevDecision follows a **modular monolithic architecture** using Spring Modulith for clear domain boundaries while maintaining deployment simplicity.

```
devdecision/
├── backend/                 # Spring Boot backend application
├── frontend/                # React frontend application
├── docker-compose.yml       # Local development infrastructure
└── README.md               # Project documentation
```

## Backend Structure (`backend/`)

### Source Organization
```
backend/src/main/java/com/devdecision/
├── DevDecisionApplication.java          # Main Spring Boot application
├── inventory/                           # Technology data management domain
│   └── package-info.java               # Module documentation
├── referee/                             # Scoring and comparison logic domain
│   └── package-info.java               # Module documentation
├── insight/                             # AI analysis and visualization domain
│   └── package-info.java               # Module documentation
└── shared/                              # Cross-cutting concerns
    └── config/                          # Application configuration
        ├── CacheConfig.java             # Redis and caching setup
        └── WebConfig.java               # CORS and web configuration
```

### Test Organization
```
backend/src/test/java/com/devdecision/
├── DevDecisionApplicationTests.java     # Application context tests
└── shared/config/                       # Configuration tests
    ├── QuickCheckSetupTest.java         # Property-based test setup
    └── QuickCheckTestBase.java          # Base class for QuickCheck tests
```

### Resources
```
backend/src/main/resources/
├── application.yml                      # Main configuration
└── application-test.yml                 # Test profile configuration
```

## Frontend Structure (`frontend/`)

### Source Organization
```
frontend/src/
├── main.tsx                            # Application entry point
├── App.tsx                             # Root component with routing
├── index.css                           # Global styles and Tailwind imports
├── vite-env.d.ts                       # Vite type definitions
├── setupTests.ts                       # Jest test configuration
├── components/                         # Reusable UI components
│   ├── Layout.tsx                      # Main layout wrapper
│   └── __tests__/                      # Component tests
│       └── Layout.test.tsx
├── contexts/                           # React contexts for global state
│   ├── ThemeContext.tsx                # Dark/light theme management
│   └── __tests__/                      # Context tests
│       └── ThemeContext.test.tsx
└── pages/                              # Page-level components
    ├── LandingPage.tsx                 # Home/landing page
    └── ComparisonDashboard.tsx         # Main comparison interface
```

### Configuration Files
```
frontend/
├── package.json                        # Dependencies and scripts
├── vite.config.ts                      # Vite build configuration
├── tsconfig.json                       # TypeScript configuration
├── tsconfig.node.json                  # Node-specific TypeScript config
├── tailwind.config.js                  # TailwindCSS theme configuration
├── postcss.config.js                   # PostCSS configuration
├── jest.config.js                      # Jest test configuration
├── .eslintrc.cjs                       # ESLint rules
└── index.html                          # HTML template
```

## Domain Module Guidelines

### Spring Modulith Modules
Each domain module (`inventory`, `referee`, `insight`) should:

- **Single Responsibility**: Focus on one business domain
- **Clear Boundaries**: Use `package-info.java` to document module purpose
- **Minimal Dependencies**: Avoid circular dependencies between modules
- **Event-Driven Communication**: Use Spring events for inter-module communication
- **Shared Module**: Common utilities and configurations in `shared/`

### Package Structure Within Modules
```
module/
├── package-info.java                   # Module documentation and boundaries
├── api/                                # REST controllers and DTOs
├── domain/                             # Core business logic and entities
├── infrastructure/                     # Data access and external integrations
└── internal/                           # Module-internal components
```

## File Naming Conventions

### Backend (Java)
- **Classes**: PascalCase (`TechnologyService.java`)
- **Packages**: lowercase with dots (`com.devdecision.inventory`)
- **Constants**: UPPER_SNAKE_CASE
- **Methods/Variables**: camelCase

### Frontend (TypeScript/React)
- **Components**: PascalCase (`ComparisonDashboard.tsx`)
- **Hooks**: camelCase starting with `use` (`useTheme.ts`)
- **Utilities**: camelCase (`apiClient.ts`)
- **Types/Interfaces**: PascalCase (`Technology.ts`)
- **Test Files**: `ComponentName.test.tsx`

## Configuration Management

### Backend Configuration
- **Environment Variables**: Use `${VAR_NAME:default}` syntax in `application.yml`
- **Profiles**: Separate configurations for `default`, `test`, `prod`
- **Secrets**: Never commit sensitive data; use environment variables

### Frontend Configuration
- **Environment Variables**: Use `VITE_` prefix for client-side variables
- **Build Configuration**: Centralized in `vite.config.ts`
- **API Endpoints**: Proxy configuration for development

## Testing Organization

### Backend Testing
- **Unit Tests**: Co-located with source in `src/test/java/`
- **Integration Tests**: Use `@SpringBootTest` with Testcontainers
- **Property-Based Tests**: Extend `QuickCheckTestBase` for comprehensive validation
- **Test Naming**: `ClassNameTest.java` or `ClassNameIntegrationTest.java`

### Frontend Testing
- **Component Tests**: `__tests__/` directories alongside components
- **Test Utilities**: Shared test setup in `setupTests.ts`
- **Mock Strategy**: Mock external dependencies, test component behavior
- **Test Naming**: `ComponentName.test.tsx`

## Development Workflow

### Local Development Setup
1. **Infrastructure**: Start with `docker-compose up -d`
2. **Backend**: Run from `backend/` directory with Maven
3. **Frontend**: Run from `frontend/` directory with npm
4. **API Communication**: Frontend proxies `/api/*` to backend

### Code Organization Principles
- **Separation of Concerns**: Clear boundaries between presentation, business logic, and data
- **Dependency Direction**: Dependencies flow inward toward business logic
- **Testability**: Design for easy unit and integration testing
- **Modularity**: Loosely coupled, highly cohesive modules