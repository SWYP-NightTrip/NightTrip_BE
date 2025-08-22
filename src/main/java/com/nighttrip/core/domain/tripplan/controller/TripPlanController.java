package com.nighttrip.core.domain.tripplan.controller;

import com.nighttrip.core.domain.tripplan.dto.TripPlanDetailResponse;
import com.nighttrip.core.domain.tripplan.dto.TripPlanResponse;
import com.nighttrip.core.domain.tripplan.dto.TripPlanStatusChangeRequest;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.tripplan.service.TripPlanService;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/plan")
@RestController
public class TripPlanController {

    private final TripPlanService tripPlanService;
    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;

    public TripPlanController(TripPlanService tripPlanService, UserRepository userRepository, TripPlanRepository tripPlanRepository) {
        this.tripPlanService = tripPlanService;
        this.userRepository = userRepository;
        this.tripPlanRepository = tripPlanRepository;
    }

    @PatchMapping("/{planId}/status")
    public ResponseEntity<ApiResponse<?>> changePlanStatus(@Valid @RequestBody TripPlanStatusChangeRequest request,
                                                           @PathVariable("planId") Long planId) {
        tripPlanService.changePlanStatus(request, planId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }

    /**
     * 현재 수정 중인 여행 계획 목록을 무한스크롤로 조회합니다.
     * @param pageable 클라이언트가 요청하는 페이지 정보 (page, size, sort)
     */
    @GetMapping("/ongoing")
    public ResponseEntity<ApiResponse<Page<TripPlanResponse>>> getOngoingTripPlans(
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {

        Page<TripPlanResponse> ongoingPlans = tripPlanService.getOngoingTripPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(ongoingPlans));
    }

    /**
     * 과거에 다녀온 여행 계획 목록을 무한스크롤로 조회합니다.
     * @param pageable 클라이언트가 요청하는 페이지 정보 (page, size, sort)
     */
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<Page<TripPlanResponse>>> getPastTripPlans(
            @PageableDefault(page = 0, size = 10, sort = "endDate") Pageable pageable) {

        Page<TripPlanResponse> pastPlans = tripPlanService.getPastTripPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(pastPlans));
    }
    /*
    @GetMapping("/{planId}/details")
    public ResponseEntity<ApiResponse<TripPlanDetailResponse>> getTripPlanDetails(
            @PathVariable("planId") Long planId) {

        TripPlanDetailResponse response = tripPlanService.getTripPlanDetails(planId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
*/
    public List<TripPlanResponse> getTripPlansByUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<TripPlan> tripPlans = tripPlanRepository.findByUserId(user.getId());

        return tripPlans.stream()
                .map(plan -> new TripPlanResponse(
                        plan.getId(),
                        plan.getTitle(),
                        plan.getStartDate(),
                        plan.getEndDate(),
                        plan.getStatus()))
                .collect(Collectors.toList());
    }
}
