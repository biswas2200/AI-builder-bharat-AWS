package com.devdecision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@Modulith
public class DevDecisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevDecisionApplication.class, args);
    }
}