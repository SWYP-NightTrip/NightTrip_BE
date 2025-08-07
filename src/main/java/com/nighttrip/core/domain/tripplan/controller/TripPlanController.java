package com.nighttrip.core.domain.tripplan.controller;

import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import com.nighttrip.core.domain.tripplan.service.TripPlanService;
import com.nighttrip.core.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/plan")
@RestController
public class TripPlanController {

    private final TripPlanService tripPlanService;

    public TripPlanController(TripPlanService tripPlanService) {
        this.tripPlanService = tripPlanService;
    }

    @PatchMapping("/{planId}/status")
    public ResponseEntity<ApiResponse<?>> changePlanStatus(@Valid @RequestBody TripPlanStatusChangeRequest request,
                                                           @PathVariable("planId") Long planId) {
        tripPlanService.changePlanStatus(request, planId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }
}
