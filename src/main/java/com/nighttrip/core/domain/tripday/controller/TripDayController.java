package com.nighttrip.core.domain.tripday.controller;

import com.nighttrip.core.domain.tripday.dto.TripPlanChangeOrderRequest;
import com.nighttrip.core.domain.tripday.service.TripDayService;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.triporder.service.TripOrderService;
import com.nighttrip.core.domain.tripplan.dto.AddPlaceRequest;
import com.nighttrip.core.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/trip-plan/add-place")
@RestController
public class TripDayController {

    private final TripDayService tripDayService;
    private final TripOrderService tripOrderService;
    public TripDayController(TripDayService tripDayService, TripOrderService tripOrderService) {
        this.tripDayService = tripDayService;
        this.tripOrderService = tripOrderService;
    }
    @PostMapping("/add-place")
    public ResponseEntity<ApiResponse<Void>> addPlace(@RequestBody AddPlaceRequest request) {
        tripOrderService.addPlace(request.tripPlanId(), request.tripDayId(), request.touristSpotIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
