package com.devdecision.shared.config;

import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;

/**
 * Test to verify QuickCheck is properly configured
 * Feature: dev-decision, Property Setup: QuickCheck Framework Integration
 */
public class QuickCheckSetupTest extends QuickCheckTestBase {
    
    @Test
    void quickCheckFrameworkIsWorking() {
        // Simple test to verify QuickCheck is properly set up
        Generator<String> stringGen = PrimitiveGenerators.strings();
        
        qt.forAll(stringGen, new AbstractCharacteristic<String>() {
            @Override
            protected void doSpecify(String s) throws Throwable {
                // Property: All strings are non-null when generated
                if (s == null) {
                    throw new AssertionError("Generated string should not be null");
                }
            }
        });
    }
    
    @Test
    void quickCheckCanGenerateIntegers() {
        Generator<Integer> intGen = PrimitiveGenerators.integers();
        
        qt.forAll(intGen, new AbstractCharacteristic<Integer>() {
            @Override
            protected void doSpecify(Integer i) throws Throwable {
                // Property: All generated integers are within valid range
                if (i == null) {
                    throw new AssertionError("Generated integer should not be null");
                }
                if (i < Integer.MIN_VALUE || i > Integer.MAX_VALUE) {
                    throw new AssertionError("Generated integer should be within valid range");
                }
            }
        });
    }
}