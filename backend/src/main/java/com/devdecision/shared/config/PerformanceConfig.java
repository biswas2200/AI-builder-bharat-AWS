package com.devdecision.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Performance optimization configuration for database connections and JPA.
 * Implements connection pooling, query optimization, and performance monitoring.
 * Only active in non-test profiles to avoid conflicts with test configurations.
 * 
 * Requirements: 8.4 - Ensure 500ms response time for up to 5 technologies
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.devdecision")
@Profile("production") // Only active in production profile, not in default, test, or standalone
public class PerformanceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${devdecision.performance.max-comparison-time-ms:500}")
    private int maxComparisonTimeMs;

    /**
     * Configure HikariCP connection pool for optimal performance.
     * Tuned for comparison workloads with up to 5 technologies.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:postgresql://localhost:5432/devdecision")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool sizing - optimized for comparison workloads
        config.setMaximumPoolSize(20); // Max concurrent connections
        config.setMinimumIdle(5);      // Keep connections ready
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);      // 10 minutes
        config.setMaxLifetime(1800000);     // 30 minutes
        
        // Performance optimizations
        config.setLeakDetectionThreshold(60000); // 1 minute leak detection
        config.setValidationTimeout(5000);       // 5 seconds validation
        config.setInitializationFailTimeout(1);  // Fail fast on startup issues
        
        // PostgreSQL-specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Connection pool name for monitoring
        config.setPoolName("DevDecisionCP");
        
        return new HikariDataSource(config);
    }

    /**
     * Configure JPA Entity Manager with performance optimizations.
     */
    @Bean
    @Primary
    @Profile("!test")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.devdecision");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Hibernate performance properties
        Properties properties = new Properties();
        
        // Basic Hibernate settings
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        
        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");
        
        // Second-level cache (disabled for now - can be enabled with Redis later)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        // properties.setProperty("hibernate.cache.region.factory_class", 
        //     "org.hibernate.cache.jcache.JCacheRegionFactory");
        
        // Connection handling
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        // Query optimization
        properties.setProperty("hibernate.query.plan_cache_max_size", "2048");
        properties.setProperty("hibernate.query.plan_parameter_metadata_max_size", "128");
        
        // Statistics for monitoring (disable in production)
        properties.setProperty("hibernate.generate_statistics", "false");
        
        em.setJpaProperties(properties);
        return em;
    }

    /**
     * Configure transaction manager for optimal performance.
     */
    @Bean
    @Primary
    @Profile("!test")
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        
        // Performance optimizations
        transactionManager.setDefaultTimeout(maxComparisonTimeMs / 1000); // Convert to seconds
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }
}