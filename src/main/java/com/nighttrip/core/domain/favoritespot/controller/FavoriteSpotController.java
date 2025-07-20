package com.nighttrip.core.domain.favoritespot.controller;

import com.nighttrip.core.domain.favoritespot.dto.FavoritePlaceAddRequest;
import com.nighttrip.core.domain.favoritespot.service.FavoriteSpotService;
import com.nighttrip.core.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/trip-plan/private-place")
@RestController
public class FavoriteSpotController {

    private final FavoriteSpotService favoriteSpotService;

    public FavoriteSpotController(FavoriteSpotService favoriteSpotService) {
        this.favoriteSpotService = favoriteSpotService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addFavoritePlace(@RequestBody FavoritePlaceAddRequest request) {
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
