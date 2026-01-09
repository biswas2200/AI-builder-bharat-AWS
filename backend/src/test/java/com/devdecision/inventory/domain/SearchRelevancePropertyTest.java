package com.devdecision.inventory.domain;

import com.devdecision.inventory.infrastructure.TechnologyRepository;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Property-based tests for search result relevance
 * Feature: dev-decision, Property 2: Search Result Relevance
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SearchRelevancePropertyTest extends QuickCheckTestBase {

    @Autowired
    private TechnologyRepository technologyRepository;

    /**
     * Property 2: Search Result Relevance
     * For any search query string, all returned technology results should contain 
     * the query term in either their name, category, or tags (case-insensitive)
     * **Validates: Requirements 1.2**
     */
    @Test
    void searchResultRelevance() {
        Generator<SearchTestData> searchDataGen = createSearchTestDataGenerator();
        
        qt.forAll(searchDataGen, new AbstractCharacteristic<SearchTestData>() {
            @Override
            protected void doSpecify(SearchTestData testData) throws Throwable {
                // First, save all technologies to ensure they exist in the database
                for (Technology tech : testData.technologies) {
                    technologyRepository.save(tech);
                }
                
                // Perform the search using the simplified method for H2 compatibility
                List<Technology> searchResults = technologyRepository.searchTechnologiesSimple(testData.query);
                
                // Verify that all results contain the query term in name, category, or tags
                for (Technology result : searchResults) {
                    boolean containsQuery = containsQueryTerm(result, testData.query);
                    
                    if (!containsQuery) {
                        throw new AssertionError(
                            String.format("Search result '%s' (category: '%s', tags: %s) does not contain query term '%s'",
                                result.getName(), result.getCategory(), result.getTags(), testData.query)
                        );
                    }
                }
            }
        });
    }

    /**
     * Creates a generator for search test data containing technologies and a query
     */
    private Generator<SearchTestData> createSearchTestDataGenerator() {
        return new Generator<SearchTestData>() {
            @Override
            public SearchTestData next() {
                // Generate a non-empty query string
                String query = generateValidQuery();
                
                // Generate 1-5 technologies, some matching the query, some not
                int numTechnologies = PrimitiveGenerators.integers(1, 5).next();
                Technology[] technologies = new Technology[numTechnologies];
                
                for (int i = 0; i < numTechnologies; i++) {
                    // 70% chance to create a technology that matches the query
                    boolean shouldMatch = PrimitiveGenerators.doubles().next() < 0.7;
                    technologies[i] = generateTechnology(query, shouldMatch, i);
                }
                
                return new SearchTestData(query, technologies);
            }
        };
    }

    /**
     * Generates a valid search query (non-empty, trimmed)
     */
    private String generateValidQuery() {
        String query;
        do {
            query = PrimitiveGenerators.strings().next();
        } while (query == null || query.trim().isEmpty() || query.length() > 50);
        
        // Ensure it's not just whitespace
        query = query.trim();
        if (query.isEmpty()) {
            query = "test"; // Fallback to ensure non-empty
        }
        
        return query;
    }

    /**
     * Generates a technology that either matches or doesn't match the query
     */
    private Technology generateTechnology(String query, boolean shouldMatch, int index) {
        String name, category;
        Set<String> tags = new HashSet<>();
        
        if (shouldMatch) {
            // Create a technology that contains the query in name, category, or tags
            int matchLocation = PrimitiveGenerators.integers(0, 2).next(); // 0=name, 1=category, 2=tags
            
            switch (matchLocation) {
                case 0: // Match in name
                    name = generateNameWithQuery(query, index);
                    category = generateRandomCategory(index);
                    break;
                case 1: // Match in category
                    name = generateRandomName(index);
                    category = generateCategoryWithQuery(query, index);
                    break;
                case 2: // Match in tags
                    name = generateRandomName(index);
                    category = generateRandomCategory(index);
                    tags.add(query.toLowerCase());
                    break;
                default:
                    name = generateNameWithQuery(query, index);
                    category = generateRandomCategory(index);
            }
        } else {
            // Create a technology that doesn't contain the query
            name = generateRandomName(index);
            category = generateRandomCategory(index);
            // Don't add the query as a tag
        }
        
        // Ensure unique names by adding index
        name = name + "_" + System.nanoTime() + "_" + index;
        
        Technology technology = new Technology(name, category);
        technology.setTags(tags);
        
        return technology;
    }

    /**
     * Generates a name that contains the query
     */
    private String generateNameWithQuery(String query, int index) {
        String[] prefixes = {"", "test_", "demo_", "sample_"};
        String[] suffixes = {"", "_lib", "_framework", "_tool"};
        
        String prefix = prefixes[index % prefixes.length];
        String suffix = suffixes[index % suffixes.length];
        
        return prefix + query + suffix;
    }

    /**
     * Generates a category that contains the query
     */
    private String generateCategoryWithQuery(String query, int index) {
        String[] prefixes = {"", "modern_", "legacy_"};
        String[] suffixes = {"", "_tools", "_frameworks"};
        
        String prefix = prefixes[index % prefixes.length];
        String suffix = suffixes[index % suffixes.length];
        
        return prefix + query + suffix;
    }

    /**
     * Generates a random name that doesn't contain the query
     */
    private String generateRandomName(int index) {
        String[] names = {"React", "Vue", "Angular", "Spring", "Django", "Express", "Laravel"};
        return names[index % names.length];
    }

    /**
     * Generates a random category that doesn't contain the query
     */
    private String generateRandomCategory(int index) {
        String[] categories = {"Frontend", "Backend", "Database", "DevOps", "Mobile", "Testing"};
        return categories[index % categories.length];
    }

    /**
     * Checks if a technology contains the query term in name, category, or tags (case-insensitive)
     */
    private boolean containsQueryTerm(Technology technology, String query) {
        String lowerQuery = query.toLowerCase();
        
        // Check name
        if (technology.getName() != null && 
            technology.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Check category
        if (technology.getCategory() != null && 
            technology.getCategory().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Check tags
        if (technology.getTags() != null) {
            for (String tag : technology.getTags()) {
                if (tag != null && tag.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Test data container for search tests
     */
    private static class SearchTestData {
        final String query;
        final Technology[] technologies;
        
        SearchTestData(String query, Technology[] technologies) {
            this.query = query;
            this.technologies = technologies;
        }
    }
}