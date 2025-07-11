package com.nighttrip.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NigthtripApplication {

    public static void main(String[] args) {
        SpringApplication.run(NigthtripApplication.class, args);
    }

}
