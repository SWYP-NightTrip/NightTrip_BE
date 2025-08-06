package com.nighttrip.core.domain.touristspot.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EnvTest {

    private final Environment environment;

    @PostConstruct
    public void printEnv() {
        System.out.println("FRONTEND_URL from env: " + environment.getProperty("FRONTEND_URL"));
        System.out.println("frontend.url from env: " + environment.getProperty("frontend.url"));
    }
}
