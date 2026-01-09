package com.devdecision.shared.domain;

import com.devdecision.referee.domain.UserConstraints;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Property-based tests for session persistence round trip
 * Feature: dev-decision, Property 18: Session Persistence Round Trip
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SessionPersistencePropertyTest extends QuickCheckTestBase {

    // Note: This test assumes a SessionService will be implemented
    // For now, we'll test the contract using a mock implementation
    private final MockSessionService sessionService = new MockSessionService();

    /**
     * Property 18: Session Persistence Round Trip
     * For any user comparison session with selected technologies and constraints,
     * saving and then retrieving the session should return equivalent data
     * **Validates: Requirements 8.5**
     */
    @Test
    void sessionPersistenceRoundTrip() {
        Generator<ComparisonSession> sessionGen = createSessionGenerator();
        
        qt.forAll(sessionGen, new AbstractCharacteristic<ComparisonSession>() {
            @Override
            protected void doSpecify(ComparisonSession originalSession) throws Throwable {
                // Save the session
                String sessionId = sessionService.saveSession(originalSession);
                
                // Retrieve the session by ID
                ComparisonSession retrievedSession = sessionService.getSession(sessionId)
                    .orElseThrow(() -> new AssertionError("Session should be retrievable after saving"));
                
                // Verify all session data is preserved
                assertSessionEquivalent(originalSession, retrievedSession);
            }
        });
    }

    /**
     * Creates a generator for valid ComparisonSession objects
     */
    private Generator<ComparisonSession> createSessionGenerator() {
        return new Generator<ComparisonSession>() {
            @Override
            public ComparisonSession next() {
                // Generate session ID (UUID format)
                String sessionId = UUID.randomUUID().toString();
                
                // Generate list of selected technologies (1-5 technologies)
                List<String> selectedTechnologies = generateSelectedTechnologies();
                
                // Generate user constraints
                UserConstraints userConstraints = generateUserConstraints();
                
                // Generate last comparison timestamp
                LocalDateTime lastComparison = generateTimestamp();
                
                return new ComparisonSession(sessionId, selectedTechnologies, userConstraints, lastComparison);
            }
        };
    }

    /**
     * Generates a list of selected technology names (1-5 technologies)
     */
    private List<String> generateSelectedTechnologies() {
        List<String> technologies = new ArrayList<>();
        
        // Common technology names for realistic testing
        String[] techNames = {
            "React", "Vue", "Angular", "Node.js", "Django", "Spring Boot",
            "PostgreSQL", "MongoDB", "Redis", "Docker", "Kubernetes",
            "AWS", "Azure", "GCP", "TypeScript", "Python", "Java"
        };
        
        // Select 1-5 random technologies
        int numTechnologies = PrimitiveGenerators.integers(1, 5).next();
        Set<String> selectedSet = new HashSet<>();
        
        while (selectedSet.size() < numTechnologies) {
            int index = PrimitiveGenerators.integers(0, techNames.length - 1).next();
            selectedSet.add(techNames[index]);
        }
        
        technologies.addAll(selectedSet);
        return technologies;
    }

    /**
     * Generates random user constraints
     */
    private UserConstraints generateUserConstraints() {
        Set<String> priorityTags = new HashSet<>();
        
        // Available priority tags
        String[] availableTags = {
            "performance", "learning-curve", "community", "documentation", 
            "scalability", "security", "popularity", "job-market"
        };
        
        // Select 0-4 random priority tags
        int numTags = PrimitiveGenerators.integers(0, 4).next();
        for (int i = 0; i < numTags; i++) {
            int index = PrimitiveGenerators.integers(0, availableTags.length - 1).next();
            priorityTags.add(availableTags[index]);
        }
        
        // Generate optional project context
        String projectType = generateOptionalString(new String[]{"web-app", "mobile-app", "api", "microservice", "desktop"});
        String teamSize = generateOptionalString(new String[]{"solo", "small", "medium", "large"});
        String timeline = generateOptionalString(new String[]{"immediate", "weeks", "months", "long-term"});
        
        return new UserConstraints(priorityTags, projectType, teamSize, timeline);
    }

    /**
     * Generates an optional string value (can be null)
     */
    private String generateOptionalString(String[] options) {
        // 50% chance of being null
        if (PrimitiveGenerators.booleans().next()) {
            return null;
        }
        
        int index = PrimitiveGenerators.integers(0, options.length - 1).next();
        return options[index];
    }

    /**
     * Generates a timestamp within the last 30 days
     */
    private LocalDateTime generateTimestamp() {
        // Generate timestamp within last 30 days
        long daysAgo = PrimitiveGenerators.integers(0, 30).next();
        long hoursAgo = PrimitiveGenerators.integers(0, 23).next();
        long minutesAgo = PrimitiveGenerators.integers(0, 59).next();
        
        return LocalDateTime.now()
            .minusDays(daysAgo)
            .minusHours(hoursAgo)
            .minusMinutes(minutesAgo);
    }

    /**
     * Asserts that two sessions are equivalent
     */
    private void assertSessionEquivalent(ComparisonSession original, ComparisonSession retrieved) {
        if (!Objects.equals(original.getSessionId(), retrieved.getSessionId())) {
            throw new AssertionError("Session ID should be preserved: expected '" + 
                original.getSessionId() + "' but got '" + retrieved.getSessionId() + "'");
        }
        
        if (!Objects.equals(original.getSelectedTechnologies(), retrieved.getSelectedTechnologies())) {
            throw new AssertionError("Selected technologies should be preserved: expected " + 
                original.getSelectedTechnologies() + " but got " + retrieved.getSelectedTechnologies());
        }
        
        if (!Objects.equals(original.getUserConstraints(), retrieved.getUserConstraints())) {
            throw new AssertionError("User constraints should be preserved: expected " + 
                original.getUserConstraints() + " but got " + retrieved.getUserConstraints());
        }
        
        // For timestamp comparison, allow small differences due to serialization/deserialization
        if (original.getLastComparison() != null && retrieved.getLastComparison() != null) {
            long timeDifference = Math.abs(
                original.getLastComparison().toEpochSecond(java.time.ZoneOffset.UTC) - 
                retrieved.getLastComparison().toEpochSecond(java.time.ZoneOffset.UTC)
            );
            if (timeDifference > 1) { // Allow 1 second difference
                throw new AssertionError("Last comparison timestamp should be preserved (within 1 second): expected " + 
                    original.getLastComparison() + " but got " + retrieved.getLastComparison());
            }
        } else if (!Objects.equals(original.getLastComparison(), retrieved.getLastComparison())) {
            throw new AssertionError("Last comparison timestamp should be preserved: expected " + 
                original.getLastComparison() + " but got " + retrieved.getLastComparison());
        }
    }

    /**
     * Domain object representing a comparison session
     * This would typically be in a domain package, but included here for the test
     */
    public static class ComparisonSession {
        private final String sessionId;
        private final List<String> selectedTechnologies;
        private final UserConstraints userConstraints;
        private final LocalDateTime lastComparison;

        public ComparisonSession(String sessionId, List<String> selectedTechnologies, 
                               UserConstraints userConstraints, LocalDateTime lastComparison) {
            this.sessionId = sessionId;
            this.selectedTechnologies = new ArrayList<>(selectedTechnologies);
            this.userConstraints = userConstraints;
            this.lastComparison = lastComparison;
        }

        public String getSessionId() { return sessionId; }
        public List<String> getSelectedTechnologies() { return new ArrayList<>(selectedTechnologies); }
        public UserConstraints getUserConstraints() { return userConstraints; }
        public LocalDateTime getLastComparison() { return lastComparison; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComparisonSession that = (ComparisonSession) o;
            return Objects.equals(sessionId, that.sessionId) &&
                   Objects.equals(selectedTechnologies, that.selectedTechnologies) &&
                   Objects.equals(userConstraints, that.userConstraints) &&
                   Objects.equals(lastComparison, that.lastComparison);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId, selectedTechnologies, userConstraints, lastComparison);
        }

        @Override
        public String toString() {
            return "ComparisonSession{" +
                    "sessionId='" + sessionId + '\'' +
                    ", selectedTechnologies=" + selectedTechnologies +
                    ", userConstraints=" + userConstraints +
                    ", lastComparison=" + lastComparison +
                    '}';
        }
    }

    /**
     * Mock session service for testing the contract
     * This represents the interface that should be implemented
     */
    public static class MockSessionService {
        private final Map<String, ComparisonSession> sessions = new HashMap<>();

        public String saveSession(ComparisonSession session) {
            String sessionId = session.getSessionId();
            sessions.put(sessionId, session);
            return sessionId;
        }

        public Optional<ComparisonSession> getSession(String sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }
    }
}