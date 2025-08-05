package com.nighttrip.core.feature.mypage.service;

import com.nighttrip.core.domain.avatar.entity.Avatar;
import com.nighttrip.core.domain.avatar.repository.AvatarRepository;
import com.nighttrip.core.domain.touristspot.entity.TourLike;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TourLikeRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.BookMarkRepository;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.feature.mypage.dto.LikedSpotDto;
import com.nighttrip.core.feature.mypage.dto.MyPageResponseDto;
import com.nighttrip.core.feature.mypage.dto.RecentPlanDto;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.TripStatus;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.image.entity.ImageSizeType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TourLikeRepository tourLikeRepository;
    private final BookMarkRepository bookMarkRepository;
    private final ImageRepository imageRepository;
    private final AvatarRepository avatarRepository;

    public MyPageResponseDto getMyPageData(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Avatar avatar = user.getAvatar();
        String avatarUrl = imageRepository.findImageSizeByTypeAndRelatedId(ImageType.AVATAR, avatar.getId(), ImageSizeType.THUMBNAIL)
                .map(ImageUrl::getUrl)
        int userLevel = (user.getAvartarLevel() != null) ? user.getAvartarLevel() : 1;

        String avatarUrl = avatarRepository.findByLevel(userLevel)
                .map(Avatar::getUri)
                .orElse(null);

        int level = (user.getAvartarLevel() != null) ? user.getAvartarLevel() : 1;

        long bookmarkedCount = bookMarkRepository.countByUser(user);
        long likedCount = tourLikeRepository.countByUser(user);

        Optional<TripPlan> representativePlanOpt = tripPlanRepository
                .findTopByUserAndStatusInOrderByUpdatedAtDesc(user, List.of(TripStatus.UPCOMING))
                .or(() -> tripPlanRepository.findTopByUserAndStatusInOrderByUpdatedAtDesc(user, List.of(TripStatus.ONGOING)));

        List<RecentPlanDto> recentPlanDtos = representativePlanOpt
                .map(this::mapToRecentPlanDto)
                .stream().toList();

        return MyPageResponseDto.builder()
                .userName(user.getNickname())
                .userAvatarUrl(avatarUrl)
                .level(level)
                .bookmarkedSpotsCount(bookmarkedCount)
                .likedSpotsCount(likedCount)
                .recentPlans(recentPlanDtos)
                .build();
    }

    public Page<LikedSpotDto> getLikedSpots(String email, Pageable pageable) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<TourLike> likedSpotsPage = tourLikeRepository.findByUserOrderByLikedAtDesc(user, pageable);

        return likedSpotsPage.map(tourLike -> {
            TouristSpot touristSpot = tourLike.getTouristSpot();

            String image = imageRepository.findImageSizeByTypeAndRelatedId(ImageType.TOURIST_SPOT, touristSpot.getId(), ImageSizeType.SEARCH)
                    .map(ImageUrl::getUrl)
                    .orElse(null);

            return LikedSpotDto.from(touristSpot, image);
        });
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

    private String extractPhotoUrlFromPlan(TripPlan plan) {
        if (plan.getTripDays().isEmpty()) return null;

        TripDay firstDay = plan.getTripDays().get(0);
        if (firstDay.getTripOrders().isEmpty()) return null;

        return firstDay.getTripOrders().stream()
                .map(TripOrder::getTouristSpot)
                .filter(Objects::nonNull)
                .findFirst().flatMap(spot -> imageRepository
                        .findImageSizeByTypeAndRelatedId(ImageType.TOURIST_SPOT, spot.getId(), ImageSizeType.SEARCH)
                        .map(ImageUrl::getUrl))
                .orElse(null);
    }
}
