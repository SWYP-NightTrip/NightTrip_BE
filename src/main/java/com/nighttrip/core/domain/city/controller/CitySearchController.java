    package com.nighttrip.core.domain.city.controller;

import com.nighttrip.core.domain.city.service.CitySearchService;
import com.nighttrip.core.domain.city.dto.CityResponseDto;
import com.nighttrip.core.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/city")
public class CitySearchController {


    private final CitySearchService citySearchService;

}
