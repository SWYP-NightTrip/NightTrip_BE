package com.nighttrip.core.domain.triporder.controller;

import com.nighttrip.core.domain.triporder.dto.PlaceAddRequest;
import com.nighttrip.core.domain.triporder.service.TripOrderService;
import com.nighttrip.core.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trip-plan/{tripPlanId}/{tripDayId}")
public class TripOrderController {

    private final TripOrderService orderService;

    public TripOrderController(TripOrderService tripOrderService) {
        this.orderService = tripOrderService;
    }


    @PostMapping("/add-place")
    public ResponseEntity<ApiResponse<?>> addPlace(@RequestBody PlaceAddRequest request,
                                                   @PathVariable("tripPlanId") Long tripPlanId,
                                                   @PathVariable("tripDayId") Integer tripDayId) {
        orderService.addPlace(request, tripPlanId, tripDayId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }

}
