package com.cookiesstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Cookies Store E-Commerce application.
 */
@SpringBootApplication
@EnableScheduling
public class CookiesStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookiesStoreApplication.class, args);
    }
}
