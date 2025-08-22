package com.nighttrip.core.domain.triporder.controller;

import com.nighttrip.core.domain.triporder.ArrivalTimeUpdateRequest;
import com.nighttrip.core.domain.triporder.service.TripOrderService;
import com.nighttrip.core.domain.tripplan.dto.TripOrderMoveRequest;
import com.nighttrip.core.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trip-order")
public class TripOrderController {

    private final TripOrderService orderService;

    public TripOrderController(TripOrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/arrival-time")
    public ResponseEntity<ApiResponse<Void>> updateArrivalTime(@RequestParam("tripOrderId") Long tripOrderId,
                                                               @RequestBody @Valid ArrivalTimeUpdateRequest request) {
        orderService.updateArrivalTime(tripOrderId, request.arrivalTime());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    @DeleteMapping("/{tripOrderId}")
    public ResponseEntity<ApiResponse<Void>> deleteTripOrder(@PathVariable("tripOrderId") Long tripOrderId) {
        orderService.deleteTripOrder(tripOrderId);
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(null));
    }
    @PutMapping("/move")
    public ResponseEntity<ApiResponse<Void>> moveTripOrder(@RequestBody @Valid TripOrderMoveRequest request) {
        orderService.moveTripOrder(
                request.movingTripOrderId(),
                request.originalTripDayId(),
                request.destinationTripDayId(),
                request.fromIndex(),
                request.toIndex()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
