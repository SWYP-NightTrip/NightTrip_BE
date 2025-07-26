package com.nighttrip.core.domain;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExController {
    @GetMapping("/api/health-check")
    public String healthCheck() {
        return "Backend is connected and healthy!";
    }
}
