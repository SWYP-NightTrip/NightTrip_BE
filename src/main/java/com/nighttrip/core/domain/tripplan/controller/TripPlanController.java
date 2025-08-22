package com.nighttrip.core.domain.tripplan.controller;

import com.nighttrip.core.domain.tripplan.dto.*;
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

    /**
     * 현재 수정 중인 여행 계획 목록을 무한스크롤로 조회합니다.
     * @param pageable 클라이언트가 요청하는 페이지 정보 (page, size, sort)
     */
    @GetMapping("/ongoing")
    public ResponseEntity<ApiResponse<Page<TripPlanResponse>>> getOngoingTripPlans(
            @PageableDefault(page = 0, size = 10, sort = "numIndex") Pageable pageable) {

        Page<TripPlanResponse> ongoingPlans = tripPlanService.getOngoingTripPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(ongoingPlans));
    }

    /**
     * 과거에 다녀온 여행 계획 목록을 무한스크롤로 조회합니다.
     * @param pageable 클라이언트가 요청하는 페이지 정보 (page, size, sort)
     */
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<Page<TripPlanResponse>>> getPastTripPlans(
            @PageableDefault(page = 0, size = 10, sort = "numIndex") Pageable pageable) {

        Page<TripPlanResponse> pastPlans = tripPlanService.getPastTripPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(pastPlans));
    }
    @GetMapping("/status")
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
    /**
     * 특정 여행 계획을 삭제합니다.
     * @param planId 삭제할 여행 계획의 ID
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deleteTripPlan(@PathVariable("planId") Long planId) {
        tripPlanService.deleteTripPlan(planId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    /**
     * 특정 여행 계획의 순서를 변경합니다.
     * UPCOMING과 ONGOING 상태는 하나의 그룹으로, COMPLETED 상태는 별도로 순서를 관리합니다.
     * @param request 순서를 변경할 여행 계획의 ID와 변경 위치 정보
     * @return 성공 응답
     */
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderTripPlan(@RequestBody TripPlanReorderRequest request) {
        tripPlanService.reorderTripPlan(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    /**
     * 현재 유저의 여행 계획 상태를 업데이트합니다.
     * UPCOMING -> ONGOING, ONGOING -> COMPLETED
     */
    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<Void>> updateTripPlanStatuses() {
        tripPlanService.updateTripPlanStatusesForUser();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    @PostMapping
    public ApiResponse<TripPlanCreateResponse> createTripPlan(@RequestBody TripPlanCreateRequest request) {
        return ApiResponse.success(tripPlanService.createTripPlan(request));
    }
}
