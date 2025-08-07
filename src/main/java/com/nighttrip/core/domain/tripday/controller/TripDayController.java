package com.nighttrip.core.domain.tripday.controller;

import com.nighttrip.core.domain.tripday.dto.TripPlanChangeOrderRequest;
import com.nighttrip.core.domain.tripday.service.TripDayService;
import com.nighttrip.core.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trip-plan/{tripPlanId}/{tripDayId}")
@RestController
public class TripDayController {

    private final TripDayService tripDayService;

    public TripDayController(TripDayService tripDayService) {
        this.tripDayService = tripDayService;
    }

    @PutMapping("change-order")
    public ResponseEntity<ApiResponse<?>> changeTripPlanOrder(@Valid @RequestBody TripPlanChangeOrderRequest request,
                                                              @PathVariable("tripPlanId") Long tripPlanId,
                                                              @PathVariable("tripDayId") Integer tripDayId) {
        tripDayService.changePlanOrder(request, tripPlanId, tripDayId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }

}
