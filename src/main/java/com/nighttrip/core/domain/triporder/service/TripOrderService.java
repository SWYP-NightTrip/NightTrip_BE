package com.nighttrip.core.domain.triporder.service;

import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripday.repository.TripDayRepository;
import com.nighttrip.core.domain.triporder.dto.PlaceAddRequest;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.tripplan.entity.TripPlan;
import com.nighttrip.core.domain.tripplan.repository.TripPlanRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ItemType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.maps.GeocodeResponse;
import com.nighttrip.core.global.maps.NaverMapFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.nighttrip.core.global.enums.ErrorCode.TRIP_PLAN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TripOrderService {

    private final TripPlanRepository tripPlanRepository;
    private final TripDayRepository tripDayRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final NaverMapFunction naverMapFunction;
    private final TripOrderRepository tripOrderRepository;

    public void addPlace(PlaceAddRequest request) {
        // 지오코딩
        String address = request.placeAddress();
        GeocodeResponse geocode = naverMapFunction.geocode(address);

        TripPlan tripPlan = tripPlanRepository.findByName(request.tripPlanName())
                .orElseThrow(() -> new BusinessException(TRIP_PLAN_NOT_FOUND));

        TripDay tripDay = tripDayRepository.findByTripPlanNameAndDayOrder(request.tripPlanName(), request.tripDayOrder())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

        Integer order = touristSpotRepository.findLastOrder(tripPlan.getId(), request.tripDayOrder())
                                .orElse(0) + 1;

        TripOrder tripOrder = new TripOrder(order, ItemType.PLACE, tripDay);
        tripOrderRepository.save(tripOrder);
    }

}
