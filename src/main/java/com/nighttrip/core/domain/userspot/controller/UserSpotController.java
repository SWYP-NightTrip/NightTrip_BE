package com.nighttrip.core.domain.userspot.controller;

import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.service.impl.UserSpotServiceImpl;
import com.nighttrip.core.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/trip-plan/private-place")
@RestController
public class UserSpotController {

    private final UserSpotServiceImpl userSpotServiceImpl;

    public UserSpotController(UserSpotServiceImpl userSpotServiceImpl) {
        this.userSpotServiceImpl = userSpotServiceImpl;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addFavoritePlace(@Valid @RequestBody UserSpotAddRequest request) {
        userSpotServiceImpl.addUserPlace(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getFavoritePlaceList() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(userSpotServiceImpl.getUserPlaceList()));
    }
}
