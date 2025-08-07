package com.nighttrip.core.domain.triporder.service.impl;

import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripday.repository.TripDayRepository;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.triporder.repository.TripOrderRepository;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ItemType;
import com.nighttrip.core.global.exception.BusinessException;
import com.nighttrip.core.global.maps.NaverMapFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TripOrderService implements com.nighttrip.core.domain.triporder.service.TripOrderService {

    private final TripDayRepository tripDayRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final NaverMapFunction naverMapFunction;
    private final TripOrderRepository tripOrderRepository;

    @Override
    public void addPlace(Long tripPlanId, Integer tripDayId) {

        TripDay tripDay = tripDayRepository.findByTripPlanIdAndTripDayId(tripPlanId, tripDayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

        BigDecimal order = touristSpotRepository.findLastOrder(tripPlanId, tripDayId)
                .orElse(BigDecimal.valueOf(0)).add(BigDecimal.valueOf(1));

        TripOrder tripOrder = new TripOrder(order, ItemType.PLACE, tripDay);
        tripOrderRepository.save(tripOrder);
    }

}
