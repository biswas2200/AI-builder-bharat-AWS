# Design Document

## Overview

DevDecision is a tech stack comparison tool built using a modular monolithic architecture with domain-driven design principles. The system consists of three core domain modules: Inventory (technology data management), Referee (scoring and comparison logic), and Insight (AI-powered analysis using Google Gemini 1.5 Flash). The frontend features a modern "Neon & Void" design system with dynamic dark/light mode support, interactive charts, and glassmorphism effects.

## Architecture

### Modular Monolith Structure

The system follows domain-centric organization rather than technical layers:

```
com.devdecision/
├── inventory/          # Technology and criteria management
│   ├── domain/
│   ├── service/
│   └── repository/
├── referee/            # Scoring engine and comparison logic
│   ├── domain/
│   ├── service/
│   └── algorithm/
├── insight/            # AI analysis and visualization data
│   ├── domain/
│   ├── service/
│   └── ai/
└── shared/             # Cross-cutting concerns
    ├── config/
    └── dto/
```

### Technology Stack

**Backend:**
- Java 21 with Spring Boot 3.2+
- Spring Modulith for module boundaries
- PostgreSQL for persistent data storage
- Redis for caching and session management
- Google Gemini 1.5 Flash for AI insights

**Frontend:**
- React 18+ with TypeScript
- Vite for build tooling
- TailwindCSS with dark mode support
- Recharts for data visualization
- Framer Motion for animations

## Components and Interfaces

### Inventory Module

**Core Entities:**
```java
@Entity
public class Technology {
    private Long id;
    private String name;
    private String category;
    private String description;
    private Map<String, Double> metrics; // Performance, popularity, etc.
    private Set<String> tags;
    private LocalDateTime createdAt;
}

@Entity
public class Criteria {
    private Long id;
    private String name;
    private String description;
    private Double weight;
    private CriteriaType type; // PERFORMANCE, LEARNING_CURVE, COMMUNITY, etc.
}
```

**Public Interface:**
```java
@Service
public interface InventoryService {
    List<Technology> findTechnologiesByCategory(String category);
    List<Technology> searchTechnologies(String query);
    List<Criteria> getAllCriteria();
    Technology findTechnologyById(Long id);
}
```

### Referee Module

**Scoring Algorithm:**
```java
@Service
public class WeightedScoringService {
    
    public ComparisonResult calculateScores(
        List<Long> technologyIds, 
        UserConstraints constraints
    ) {
        // 1. Fetch technologies from Inventory
        // 2. Apply user-selected tag multipliers (1.5x boost)
        // 3. Normalize scores to 0-100 scale
        // 4. Generate radar chart data
        // 5. Calculate overall rankings
    }
    
    private Double applyTagMultipliers(Double baseScore, Set<String> userTags, String criteriaType) {
        return userTags.contains(criteriaType.toLowerCase()) ? baseScore * 1.5 : baseScore;
    }
}
```

**Domain Objects:**
```java
public record UserConstraints(
    Set<String> priorityTags,
    String projectType,
    String teamSize,
    String timeline
) {}

public record ComparisonResult(
    List<TechnologyScore> scores,
    RadarChartData radarData,
    List<KpiMetric> kpiMetrics,
    String recommendationSummary
) {}
```

### Insight Module with Gemini Integration

**AI Service Implementation:**
```java
@Service
public class GeminiService {
    
    private final VertexAI vertexAI;
    private final GenerativeModel model;
    
    public ComparisonResult generateInsights(List<String> technologyNames) {
        try {
            String prompt = buildAnalysisPrompt(technologyNames);
            GenerateContentResponse response = model.generateContent(prompt);
            return parseJsonResponse(response.getText());
        } catch (Exception e) {
            log.warn("Gemini API failed, returning cached data", e);
            return getFallbackData(technologyNames);
        }
    }
    
    private String buildAnalysisPrompt(List<String> technologies) {
        return """
            You are a Senior Tech Lead. Analyze these technologies: %s
            
            Return the response in strict JSON format with no markdown formatting.
            The JSON must contain:
            - 'radarChartData': array with fields: subject, A, B, fullMark
            - 'insights': array of strength/weakness analysis
            - 'tradeOffs': key decision factors
            - 'recommendations': personalized advice
            
            JSON Schema: %s
            """.formatted(String.join(", ", technologies), getJsonSchema());
    }
}
```

**Fallback Mechanism:**
```java
@Component
public class InsightFallbackService {
    
    public ComparisonResult getFallbackData(List<String> technologies) {
        // Return pre-seeded mock data to ensure UI never breaks
        return ComparisonResult.builder()
            .radarData(generateMockRadarData(technologies))
            .insights(generateMockInsights(technologies))
            .build();
    }
}
```

## Data Models

### Database Schema

**Technologies Table:**
```sql
CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    github_stars INTEGER,
    npm_downloads BIGINT,
    job_openings INTEGER,
    satisfaction_score DECIMAL(3,1),
    performance_score DECIMAL(3,1),
    learning_curve_score DECIMAL(3,1),
    community_score DECIMAL(3,1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Criteria Table:**
```sql
CREATE TABLE criteria (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    weight DECIMAL(3,2) DEFAULT 1.0,
    criteria_type VARCHAR(50) NOT NULL
);
```

**User Sessions (Redis):**
```json
{
  "sessionId": "uuid",
  "selectedTechnologies": ["react", "vue", "angular"],
  "userConstraints": {
    "priorityTags": ["performance", "learning-curve"],
    "projectType": "web-app"
  },
  "lastComparison": "2024-01-09T10:30:00Z"
}
```

### Frontend Data Models

**TypeScript Interfaces:**
```typescript
interface Technology {
  id: number;
  name: string;
  category: string;
  description: string;
  metrics: Record<string, number>;
  tags: string[];
}

interface RadarChartData {
  subject: string;
  A: number;
  B: number;
  C?: number;
  fullMark: number;
}

interface ComparisonResult {
  technologies: Technology[];
  radarData: RadarChartData[];
  kpiMetrics: KpiMetric[];
  insights: InsightAnalysis[];
  recommendations: string;
}
```

## UI Design System: "Neon & Void"

### Theme Configuration

**Tailwind Config:**
```javascript
module.exports = {
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        purple: {
          50: '#faf5ff',
          400: '#c084fc',
          500: '#a855f7',
          900: '#581c87'
        },
        slate: {
          950: '#020617'
        }
      }
    }
  }
}
```

### Component Styling

**Light Mode:**
- Background: `bg-purple-50` to `bg-white` gradients
- Text: `text-purple-900` for primary content
- Accents: `#6366f1` for interactive elements
- Cards: `bg-white/70` with `backdrop-blur-md`

**Dark Mode:**
- Background: `bg-slate-950` void backgrounds
- Borders: `border-purple-500/50` glowing effects
- Text: `text-purple-400` neon styling
- Cards: `bg-white/10` with `backdrop-blur-md`

**Interactive Effects:**
```css
.comparison-card {
  @apply transition-all duration-300 hover:-translate-y-1;
  box-shadow: 0 10px 25px rgba(168, 85, 247, 0.15);
}

.search-input:focus {
  @apply ring-2 ring-purple-500 ring-opacity-50;
}
```

### Layout Components

**Landing Page:**
- Hero section with centered alignment
- Floating search input with purple glow on focus
- Example comparison cards in grid layout
- Aurora effect background in dark mode

**Dashboard Layout:**
- Sticky sidebar ("The Referee") with comparison controls
- Main content area with charts and KPI cards
- Glassmorphism cards with subtle animations
- Responsive grid system for different screen sizes

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, several properties can be consolidated to eliminate redundancy:
- Properties related to CSS class application (5.2, 5.3, 5.5, 6.2, 6.3, 6.4) can be grouped as UI state properties
- Data persistence properties (1.1, 8.1, 8.5) share similar round-trip validation patterns
- Chart generation properties (3.2, 3.5) both validate data structure formatting

### Core Properties

**Property 1: Technology Data Round Trip**
*For any* valid technology object with name, category, metrics, and tags, storing it in the system and then retrieving it should return an equivalent object with all properties preserved
**Validates: Requirements 1.1, 8.1**

**Property 2: Search Result Relevance**
*For any* search query string, all returned technology results should contain the query term in either their name, category, or tags (case-insensitive)
**Validates: Requirements 1.2**

**Property 3: Technology Display Completeness**
*For any* technology being displayed, the rendered output should include category, description, and key characteristics fields
**Validates: Requirements 1.4**

**Property 4: Custom Technology Persistence**
*For any* user-created technology with valid metadata, adding it to the system should make it retrievable through search and display functions
**Validates: Requirements 1.5**

**Property 5: Weighted Scoring Consistency**
*For any* set of technologies and criteria, calculating scores multiple times with identical inputs should produce identical results
**Validates: Requirements 2.1**

**Property 6: Tag Multiplier Application**
*For any* technology score and selected importance tag, when the tag matches a criterion type, the corresponding score should be exactly 1.5 times the base score
**Validates: Requirements 2.2**

**Property 7: Score Normalization Range**
*For any* calculated technology scores, all criterion values should fall within the range 0 to 100 inclusive
**Validates: Requirements 2.4**

**Property 8: Custom Criteria Integration**
*For any* user-defined criterion with manual scoring, it should be usable in the scoring algorithm and produce valid normalized results
**Validates: Requirements 2.5**

**Property 9: Radar Chart Data Structure**
*For any* set of compared technologies, the generated radar chart data should contain subject, technology score fields (A, B, C, etc.), and fullMark values for each criterion
**Validates: Requirements 3.2**

**Property 10: KPI Metrics Completeness**
*For any* technology in a comparison, its KPI card should include GitHub stars, NPM downloads, job openings, and satisfaction score metrics
**Validates: Requirements 3.3**

**Property 11: Historical Trend Data Format**
*For any* technology with historical data, the trend chart data should be properly formatted with time series points and metric values
**Validates: Requirements 3.5**

**Property 12: AI Analysis Generation**
*For any* list of technology names, the insight module should generate structured analysis containing strengths, weaknesses, and explanatory content
**Validates: Requirements 4.1**

**Property 13: Personalized Recommendations**
*For any* comparison with user constraints, the system should generate recommendations that reference the specified constraints and selected technologies
**Validates: Requirements 4.2**

**Property 14: Trade-off Identification**
*For any* set of compared technologies, the system should identify and structure key trade-offs between the options
**Validates: Requirements 4.3**

**Property 15: Decision Support Questions**
*For any* comparison session, the system should generate relevant questions formatted as actionable decision criteria
**Validates: Requirements 4.4**

**Property 16: Scoring Explanations**
*For any* technology comparison result, explanations should be provided for why certain technologies score higher in specific use cases
**Validates: Requirements 4.5**

**Property 17: Data Caching Behavior**
*For any* frequently accessed technology data, subsequent requests should retrieve the data from cache when available, reducing database queries
**Validates: Requirements 8.2**

**Property 18: Session Persistence Round Trip**
*For any* user comparison session with selected technologies and constraints, saving and then retrieving the session should return equivalent data
**Validates: Requirements 8.5**

## Error Handling

### API Error Responses
- **Invalid Technology IDs**: Return 404 with descriptive error message
- **Malformed Comparison Requests**: Return 400 with validation details
- **Gemini API Failures**: Gracefully fallback to cached mock data
- **Database Connection Issues**: Return 503 with retry-after header
- **Redis Cache Failures**: Continue operation with database-only mode

### Frontend Error Boundaries
- **Chart Rendering Failures**: Display placeholder with error message
- **Theme Toggle Issues**: Fallback to system preference
- **Search API Timeouts**: Show cached results with warning indicator
- **Session Restoration Failures**: Start fresh session with notification

### Gemini Integration Resilience
```java
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public ComparisonResult generateInsights(List<String> technologies) {
    try {
        return callGeminiAPI(technologies);
    } catch (Exception e) {
        log.warn("Gemini API attempt failed: {}", e.getMessage());
        throw e; // Retry will handle this
    }
}

@Recover
public ComparisonResult recoverFromGeminiFailure(Exception ex, List<String> technologies) {
    log.error("All Gemini API attempts failed, using fallback data", ex);
    return insightFallbackService.getFallbackData(technologies);
}
```

## Testing Strategy

### Dual Testing Approach

The system requires both unit tests and property-based tests for comprehensive coverage:

**Unit Tests** focus on:
- Specific examples demonstrating correct behavior
- Edge cases and error conditions  
- Integration points between modules
- UI component rendering with specific props

**Property-Based Tests** focus on:
- Universal properties that hold across all inputs
- Comprehensive input coverage through randomization
- Correctness properties defined in this design document

### Property-Based Testing Configuration

**Framework**: Use **QuickCheck for Java** (net.java.quickcheck) for property-based testing
- Minimum 100 iterations per property test
- Each test references its design document property
- Tag format: **Feature: dev-decision, Property {number}: {property_text}**

**Example Property Test Structure**:
```java
@Test
public void testTechnologyDataRoundTrip() {
    // Feature: dev-decision, Property 1: Technology Data Round Trip
    qt().forAll(technologyGenerator())
        .check((technology) -> {
            Technology stored = inventoryService.saveTechnology(technology);
            Technology retrieved = inventoryService.findById(stored.getId());
            return technology.equals(retrieved);
        });
}
```

### Unit Testing Focus Areas

**Backend Unit Tests**:
- Module interface contracts
- Scoring algorithm edge cases (empty inputs, single technology)
- Database entity validation
- Gemini service fallback scenarios

**Frontend Unit Tests**:
- Component rendering with different theme states
- Chart component data transformation
- Search input validation and formatting
- Theme toggle state management

### Integration Testing

**API Integration Tests**:
- End-to-end comparison workflows
- Database seeding verification
- Redis caching behavior validation
- Gemini API integration with mock responses

**UI Integration Tests**:
- Complete user comparison journeys
- Theme switching across all components
- Chart rendering with real data
- Responsive layout behavior

Both testing approaches are essential: unit tests catch concrete bugs and validate specific scenarios, while property-based tests verify general correctness across the entire input space.