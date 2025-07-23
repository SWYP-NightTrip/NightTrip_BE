package com.nighttrip.core.domain;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExController {
    @GetMapping("/api/health-check") // GET 요청이 "/api/health-check" 경로로 들어오면 이 메서드가 처리합니다.
    public String healthCheck() {
        return "Backend is connected and healthy!";
    }
}
