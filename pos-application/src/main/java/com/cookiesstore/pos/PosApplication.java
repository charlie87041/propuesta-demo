package com.cookiesstore.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.cookiesstore.pos")
@EntityScan(basePackages = "com.cookiesstore.pos.domain")
@EnableJpaRepositories(basePackages = "com.cookiesstore.pos.repository")
public class PosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}
