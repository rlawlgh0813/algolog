package com.algolog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AlgologApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgologApplication.class, args);
    }

}
