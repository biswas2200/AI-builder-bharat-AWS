package com.devdecision.shared.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Performance monitoring aspect to track response times and ensure SLA compliance.
 * Monitors comparison operations to ensure 500ms response time requirement.
 * 
 * Requirements: 8.4 - Ensure 500ms response time for up to 5 technologies
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

    @Value("${devdecision.performance.max-comparison-time-ms:500}")
    private int maxComparisonTimeMs;

    /**
     * Monitor comparison controller methods for performance.
     */
    @Around("execution(* com.devdecision.api.ComparisonController.generate*(..))")
    public Object monitorComparisonPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log performance metrics
            performanceLogger.info("COMPARISON_PERFORMANCE: method={}, duration={}ms, sla_met={}", 
                methodName, duration, duration <= maxComparisonTimeMs);
            
            // Warn if SLA is violated
            if (duration > maxComparisonTimeMs) {
                logger.warn("SLA VIOLATION: {} took {}ms, exceeds {}ms limit", 
                    methodName, duration, maxComparisonTimeMs);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            performanceLogger.error("COMPARISON_ERROR: method={}, duration={}ms, error={}", 
                methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor service layer methods for performance insights.
     */
    @Around("execution(* com.devdecision.*.api.*Service.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log service performance (debug level)
            logger.debug("SERVICE_PERFORMANCE: service={}, method={}, duration={}ms", 
                className, methodName, duration);
            
            // Warn on slow service calls (>100ms)
            if (duration > 100) {
                logger.warn("SLOW_SERVICE: {}.{} took {}ms", className, methodName, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("SERVICE_ERROR: service={}, method={}, duration={}ms, error={}", 
                className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor database repository methods for query performance.
     */
    @Around("execution(* com.devdecision.*.infrastructure.*Repository.*(..))")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log repository performance (trace level)
            logger.trace("REPOSITORY_PERFORMANCE: repository={}, method={}, duration={}ms", 
                className, methodName, duration);
            
            // Warn on slow queries (>50ms)
            if (duration > 50) {
                logger.warn("SLOW_QUERY: {}.{} took {}ms", className, methodName, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("REPOSITORY_ERROR: repository={}, method={}, duration={}ms, error={}", 
                className, methodName, duration, e.getMessage());
            throw e;
        }
    }
}