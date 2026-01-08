package com.devdecision.shared.config;

import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for property-based tests using QuickCheck
 * Provides common setup and utilities for property testing
 */
public abstract class QuickCheckTestBase {
    
    protected QuickCheck qt;
    
    @BeforeEach
    void setupQuickCheck() {
        qt = new QuickCheck();
    }
    
    /**
     * Helper method to create a characteristic that always passes
     * Useful for testing the QuickCheck setup
     */
    protected AbstractCharacteristic<Object> alwaysTrue() {
        return new AbstractCharacteristic<Object>() {
            @Override
            protected void doSpecify(Object any) throws Throwable {
                // Always passes - used for setup verification
            }
        };
    }
}