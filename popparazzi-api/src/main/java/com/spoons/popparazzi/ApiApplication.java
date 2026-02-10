package com.spoons.popparazzi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) {
        log.info("Application started.");
        SpringApplication.run(ApiApplication.class, args);
    }
}
