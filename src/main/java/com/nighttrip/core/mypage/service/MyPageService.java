package com.nighttrip.core.mypage.service;

import com.nighttrip.core.domain.avatar.entity.Avatar;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotImageUri;
import com.nighttrip.core.domain.touristspot.repository.TourLikeRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.BookMarkRepository;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.mypage.dto.MyPageResponseDto;
import com.nighttrip.core.mypage.dto.RecentPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TourLikeRepository tourLikeRepository;
    private final BookMarkRepository bookMarkRepository;

    public MyPageResponseDto getMyPageData(String email) {
        // 1. 이메일로 유저 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 유저의 아바타 정보 조회
        Avatar avatar = user.getAvatar();
        String avatarUrl = (avatar != null) ? avatar.getImageUrl() : null;
        int level = (avatar != null) ? avatar.getLevel() : 1;

        // 3. 북마크 및 좋아요 수 계산
        long bookmarkedCount = bookMarkRepository.countByUser(user);
        long likedCount = tourLikeRepository.countByUser(user);

        // 4. 최근 여행 계획 조회
        List<TripPlan> recentTripPlans = tripPlanRepository.findTop3ByUserOrderByCreatedAtDesc(user);

        List<RecentPlanDto> recentPlanDtos = recentTripPlans.stream()
                .map(this::mapToRecentPlanDto)
                .collect(Collectors.toList());

        // 5. 최종 DTO 조립 후 반환
        return MyPageResponseDto.builder()
                .userName(user.getNickname())
                .userAvatarUrl(avatarUrl)
                .level(level)
                .bookmarkedSpotsCount(bookmarkedCount)
                .likedSpotsCount(likedCount)
                .recentPlans(recentPlanDtos)
                .build();
    }

    private RecentPlanDto mapToRecentPlanDto(TripPlan plan) {
        return RecentPlanDto.builder()
                .planId(plan.getId())
                .planTitle(plan.getTitle())
                .planPhotoUrl(extractPhotoUrlFromPlan(plan))
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .build();
    }

    // 여행 계획의 대표 이미지를 추출하는 로직 (예: 첫날 첫번째 관광지 이미지)
    private String extractPhotoUrlFromPlan(TripPlan plan) {
        if (plan.getTripDays().isEmpty()) return null;

        TripDay firstDay = plan.getTripDays().get(0);
        if (firstDay.getTripOrders().isEmpty()) return null;

        return firstDay.getTripOrders().stream()
                .flatMap(order -> order.getTouristSpots().stream())
                .findFirst()
                .flatMap(spot -> spot.getTouristSpotImageUris().stream()
                        .filter(TouristSpotImageUri::isMain)
                        .findFirst()
                        .or(() -> spot.getTouristSpotImageUris().stream().findFirst())
                )
                .map(TouristSpotImageUri::getUri)
                .orElse(null);
    }
}
