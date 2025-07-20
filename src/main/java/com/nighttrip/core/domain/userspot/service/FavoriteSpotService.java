package com.nighttrip.core.domain.userspot.service;

import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.dto.UserSpotListResponse;
import com.nighttrip.core.domain.userspot.entity.UserSpot;
import com.nighttrip.core.domain.userspot.repository.UserSpotRepository;
import com.nighttrip.core.domain.user.entity.User;
import com.nighttrip.core.domain.user.repository.UserRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.maps.GeocodeResponse;
import com.nighttrip.core.global.maps.NaverMapFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteSpotService {

    private final UserRepository userRepository;
    private final UserSpotRepository userSpotRepository;
    private final NaverMapFunction naverMapFunction;

    public void addFavoritePlace(UserSpotAddRequest request) {
        if (userSpotRepository.existsByUserIdAndSpotName(0L, request.placeName())) {
            throw new BusinessException(ErrorCode.FAVORITE_PLACE_IS_ALREADY_CREATED);
        }

        User user = userRepository.findById(0L)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GeocodeResponse geocode = naverMapFunction.geocode(request.placeAddress());

        UserSpot spot = new UserSpot(user, request.placeName(), request.placeExplain(), geocode.y(), geocode.x(), request.imageUrl(),request.category());
        userSpotRepository.save(spot);
    }

    public ArrayList<UserSpotListResponse> getFavoritePlaceList() {
        List<UserSpot> favoriteSpots = userSpotRepository.findByUserId(0L);

        return favoriteSpots.stream()
                .map(UserSpotListResponse::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
