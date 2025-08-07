package com.nighttrip.core.domain.userspot.service;

import com.nighttrip.core.domain.userspot.dto.UserSpotAddRequest;
import com.nighttrip.core.domain.userspot.dto.UserSpotListResponse;

import java.util.List;

public interface UserSpotService {
    void addUserPlace(UserSpotAddRequest request);

    List<UserSpotListResponse> getUserPlaceList();
}
