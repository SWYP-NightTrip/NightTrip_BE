package com.nighttrip.core.domain.userspot.service;

import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.dto.UserSpotListResponse;
import com.nighttrip.core.domain.userspot.entity.UserSpot;
import com.nighttrip.core.domain.userspot.repository.UserSpotRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageType;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserSpotService {

    private final UserRepository userRepository;
    private final UserSpotRepository userSpotRepository;
    private final ImageRepository imageRepository;
    private final NaverMapFunction naverMapFunction;

    public void addFavoritePlace(UserSpotAddRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userSpotRepository.existsByUserIdAndSpotName(user.getId(), request.placeName())) {
            throw new BusinessException(ErrorCode.FAVORITE_PLACE_IS_ALREADY_CREATED);
        }

        GeocodeResponse geocode = naverMapFunction.geocode(request.placeAddress());

        UserSpot spot = userSpotRepository.save(new UserSpot(user, request.placeName(), request.placeExplain(), geocode.y(), geocode.x(), request.category()));

        List<ImageUrl> images = IntStream.range(0, request.imageUrl().size())
                .mapToObj(i -> {
                    String url = request.imageUrl().get(i);
                    boolean isMain = (i == 0); // 첫 번째 이미지면 대표

                    return new ImageUrl(
                            ImageType.USER_SPOT,
                            spot.getId(),
                            url,
                            ImageSizeType.THUMBNAIL
                    );
                })
                .collect(Collectors.toList());

        imageRepository.saveAll(images);

    }

    public List<UserSpotListResponse> getFavoritePlaceList() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserSpot> favoriteSpots = userSpotRepository.findByUserId(user.getId());

        return favoriteSpots.stream()
                .map(userSpot -> {
                            String imageUrl = imageRepository.findSEARCHImage(String.valueOf(ImageType.USER_SPOT), userSpot.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return new UserSpotListResponse(userSpot, imageUrl);
                        }
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
