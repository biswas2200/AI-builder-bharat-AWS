-- DevDecision Database Schema
-- This file documents the expected database schema for reference
-- Actual tables are created by Hibernate JPA annotations

-- Technologies table
-- CREATE TABLE technologies (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(100) NOT NULL UNIQUE,
--     category VARCHAR(50) NOT NULL,
--     description TEXT,
--     metrics JSONB,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- Technology tags table (created by @ElementCollection)
-- CREATE TABLE technology_tags (
--     technology_id BIGINT NOT NULL,
--     tag VARCHAR(255),
--     FOREIGN KEY (technology_id) REFERENCES technologies(id)
-- );

-- Criteria table
-- CREATE TABLE criteria (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     description TEXT,
--     weight DECIMAL(3,2) DEFAULT 1.0,
--     criteria_type VARCHAR(50) NOT NULL
-- );

-- Indexes for performance
-- CREATE INDEX idx_technology_name ON technologies(name);
-- CREATE INDEX idx_technology_category ON technologies(category);
-- CREATE INDEX idx_criteria_name ON criteria(name);
-- CREATE INDEX idx_criteria_type ON criteria(criteria_type);

-- Note: This is a documentation file. 
-- Actual schema creation is handled by Hibernate with ddl-auto: update
-- This ensures JPA annotations remain the single source of truth for schema definition