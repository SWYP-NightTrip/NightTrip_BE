package com.nighttrip.core.domain.triporder.service.impl;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.domain.tripday.entity.TripDay;
import com.nighttrip.core.domain.tripday.repository.TripDayRepository;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.triporder.repository.TripOrderRepository;
import com.nighttrip.core.domain.triporder.service.TripOrderService;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ItemType;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class TripOrderServiceImpl implements TripOrderService {

    private final TripDayRepository tripDayRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final TripOrderRepository tripOrderRepository;

    @Transactional
    public void updateArrivalTime(Long tripOrderId, String arrivalTime) {
        TripOrder tripOrder = tripOrderRepository.findById(tripOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_ORDER_NOT_FOUND));
        String formattedTime = parseAndFormatTime(arrivalTime);
        tripOrder.changeArrivalTime(formattedTime);
    }

    private String parseAndFormatTime(String arrivalTime) {
        if (arrivalTime == null || arrivalTime.isBlank()) {
            return null;
        }
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("a h시 mm분");
            LocalTime parsedTime = LocalTime.parse(arrivalTime, inputFormatter);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return parsedTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_TIME_FORMAT);
        }
    }
    @Transactional
    public void deleteTripOrder(Long tripOrderId) {
        TripOrder tripOrder = tripOrderRepository.findById(tripOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_ORDER_NOT_FOUND));
        tripOrderRepository.delete(tripOrder);
    }

    @Transactional
    public void moveTripOrder(Long movingTripOrderId, Long originalTripDayId, Long destinationTripDayId, int fromIndex, int toIndex) {
        TripOrder movingTripOrder = tripOrderRepository.findById(movingTripOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_ORDER_NOT_FOUND));

        TripDay originalTripDay = tripDayRepository.findById(originalTripDayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

        if (originalTripDayId.equals(destinationTripDayId)) {
            if (fromIndex < toIndex) {
                tripOrderRepository.decrementOrderIndex(originalTripDayId, fromIndex + 1, toIndex);
            } else {
                tripOrderRepository.incrementOrderIndex(originalTripDayId, toIndex, fromIndex - 1);
            }

            movingTripOrder.changeOrderIndex(Long.valueOf(toIndex));
        } else {
            TripDay destinationTripDay = tripDayRepository.findById(destinationTripDayId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

            originalTripDay.removeTripOrder(movingTripOrder);
            tripOrderRepository.decrementOrderIndex(originalTripDayId, fromIndex + 1, originalTripDay.getTripOrders().size());
            movingTripOrder.setTripDay(destinationTripDay); // TripOrder.java에 setTripDay 메서드 추가 필요
            destinationTripDay.addTripOrderAt(movingTripOrder, toIndex); // TripDay.java에 addTripOrderAt 메서드 추가 필요
            tripOrderRepository.incrementOrderIndex(destinationTripDayId, toIndex, destinationTripDay.getTripOrders().size() - 1);



            movingTripOrder.changeOrderIndex(Long.valueOf(toIndex));
        }
    }
    @Override
    public void addPlace(Long tripPlanId, Integer tripDayId, Long touristSpotId) {
        TripDay tripDay = tripDayRepository.findByTripPlanIdAndTripDayId(tripPlanId, tripDayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND));

        TouristSpot touristSpot = touristSpotRepository.findById(touristSpotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        Long order = tripOrderRepository.findLastOrder(tripPlanId, tripDayId)
                .orElse(0L) + 1L;

        TripOrder tripOrder = TripOrder.builder()
                .orderIndex(order)
                .itemType(ItemType.PLACE)
                .tripDay(tripDay)
                .touristSpot(touristSpot)
                .build();

        tripOrderRepository.save(tripOrder);
    }

}
