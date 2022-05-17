package com.snomed.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SnomedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnomedApplication.class, args);
    }

}
