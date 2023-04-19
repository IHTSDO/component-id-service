package org.snomed.cis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CisApplication {

    public static void main(String[] args) {
        SpringApplication.run(CisApplication.class, args);
    }

}
