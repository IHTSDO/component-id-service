package org.snomed.cis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnomedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnomedApplication.class, args);
    }

}
