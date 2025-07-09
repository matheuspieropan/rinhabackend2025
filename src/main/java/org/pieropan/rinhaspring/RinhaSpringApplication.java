package org.pieropan.rinhaspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RinhaSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinhaSpringApplication.class, args);
    }
}