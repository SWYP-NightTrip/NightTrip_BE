package com.nighttrip.core.domain.triporder.controller;

import com.nighttrip.core.domain.triporder.service.impl.TripOrderService;
import com.nighttrip.core.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trip-plan")
public class TripOrderController {

    private final TripOrderService orderService;

    public TripOrderController(TripOrderService tripOrderService) {
        this.orderService = tripOrderService;
    }


    @PostMapping("/{tripPlanId}/{tripDayId}/add-place")
    public ResponseEntity<ApiResponse<?>> addPlace(@PathVariable("tripPlanId") Long tripPlanId,
                                                   @PathVariable("tripDayId") Integer tripDayId) {
        orderService.addPlace(tripPlanId, tripDayId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }

}
