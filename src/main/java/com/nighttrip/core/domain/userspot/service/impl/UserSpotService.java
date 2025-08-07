package com.nighttrip.core.domain.userspot.service.impl;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.dto.UserSpotListResponse;
import com.nighttrip.core.domain.userspot.entity.UserSpot;
import com.nighttrip.core.domain.userspot.repository.UserSpotRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.SpotDetails;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.image.entity.ImageSizeType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.maps.GeocodeResponse;
import com.nighttrip.core.global.maps.NaverMapFunction;
import com.nighttrip.core.global.oauth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSpotService implements com.nighttrip.core.domain.userspot.service.UserSpotService {

    private final UserRepository userRepository;
    private final UserSpotRepository userSpotRepository;
    private final ImageRepository imageRepository;
    private final NaverMapFunction naverMapFunction;

    @Override
    public void addUserPlace(UserSpotAddRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userSpotRepository.existsByUserIdAndSpotName(user.getId(), request.placeName())) {
            throw new BusinessException(ErrorCode.FAVORITE_PLACE_IS_ALREADY_CREATED);
        }

        GeocodeResponse geocode = naverMapFunction.geocode(request.placeAddress());

        EnumSet<SpotDetails> detailsEnumSet = getSpotDetails(request);

        UserSpot spot = userSpotRepository.save(new UserSpot(user, request.placeName(), request.placeAddress(), request.placeExplain(), geocode.y(), geocode.x(), SpotCategory.valueOf(request.category()), detailsEnumSet));

        saveThumbnailImage(request, spot);

        saveImages(request, spot);
    }

    private void saveImages(UserSpotAddRequest request, UserSpot spot) {
        List<String> rawImages = request.imageUrl();

        if (rawImages != null && !rawImages.isEmpty()) {
            List<ImageUrl> images = rawImages.stream()
                    .map(s -> new ImageUrl(
                            ImageType.USER_SPOT,
                            spot.getId(),
                            s,
                            ImageSizeType.DETAIL
                    ))
                    .collect(Collectors.toList());

            imageRepository.saveAll(images);
        }
    }

    private EnumSet<SpotDetails> getSpotDetails(UserSpotAddRequest request) {
        List<String> details = request.details();

        return (details == null || details.isEmpty())
                ? EnumSet.noneOf(SpotDetails.class)
                : details.stream()
                .map(SpotDetails::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SpotDetails.class)));
    }

    private void saveThumbnailImage(UserSpotAddRequest request, UserSpot spot) {
        ImageUrl thumbnail = new ImageUrl(ImageType.USER_SPOT, spot.getId(), request.thumbnailUrl(), ImageSizeType.THUMBNAIL);
        imageRepository.save(thumbnail);
    }

    @Override
    public List<UserSpotListResponse> getUserPlaceList() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserSpot> userSpots = userSpotRepository.findByUserId(user.getId());

        return userSpots.stream()
                .map(userSpot -> {
                            String imageUrl = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.USER_SPOT), userSpot.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return new UserSpotListResponse(userSpot, imageUrl);
                        }
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
