package com.nighttrip.core.domain.userspot.controller;

import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.service.FavoriteSpotService;
import com.nighttrip.core.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/trip-plan/private-place")
@RestController
public class UserSpotController {

    private final FavoriteSpotService favoriteSpotService;

    public UserSpotController(FavoriteSpotService favoriteSpotService) {
        this.favoriteSpotService = favoriteSpotService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addFavoritePlace(@RequestBody UserSpotAddRequest request) {
        favoriteSpotService.addFavoritePlace(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getFavoritePlaceList() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(favoriteSpotService.getFavoritePlaceList()));
    }
}
