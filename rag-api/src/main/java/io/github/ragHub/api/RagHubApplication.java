package io.github.ragHub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.ragHub")
public class RagHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagHubApplication.class, args);
    }
}
