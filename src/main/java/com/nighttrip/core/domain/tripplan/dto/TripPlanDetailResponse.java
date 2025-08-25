package com.nighttrip.core.domain.tripplan.dto;


import com.nighttrip.core.domain.touristspot.entity.TouristSpot;

import java.util.List;

public record TripPlanDetailResponse(
        Long tripPlanId,
        List<CityDto> cities,
        List<TripDayDto> tripDays
) {

    public record CityDto(Long id, String name) {
        public static CityDto from(com.nighttrip.core.domain.city.entity.City city) {
            return new CityDto(city.getId(), city.getCityName());
        }
    }

    public record TripDayDto(
            Long id,
            Integer dayOrder,
            List<TripOrderDto> tripOrders
    ) {
        public static TripDayDto from(com.nighttrip.core.domain.tripday.entity.TripDay tripDay) {
            List<TripOrderDto> orders = tripDay.getTripOrders().stream()
                    .map(TripOrderDto::from)
                    .toList();
            return new TripDayDto(tripDay.getId(), tripDay.getDayOrder(), orders);
        }
    }

    public record TripOrderDto(
            Long id,
            Long orderIndex,
            String arrivalTime,
            String itemType,
            TouristSpotDto touristSpot
    ) {
        public static TripOrderDto from(com.nighttrip.core.domain.triporder.entity.TripOrder tripOrder) {
            TouristSpotDto tsDto = tripOrder.getTouristSpot() != null
                    ? TouristSpotDto.from(tripOrder.getTouristSpot())
                    : null;

            return new TripOrderDto(
                    tripOrder.getId(),
                    tripOrder.getOrderIndex(),
                    tripOrder.getArrivalTime(),
                    tripOrder.getItemType().name(),
                    tsDto
            );
        }
    }

    public record TouristSpotDto(
            Long id,
            String name,
            String address,
            Long cityId,
            String cityName
    ) {
        public static TouristSpotDto from(TouristSpot spot) {
            return new TouristSpotDto(
                    spot.getId(),
                    spot.getSpotName(),
                    spot.getAddress(),
                    spot.getCity() != null ? spot.getCity().getId() : null,
                    spot.getCity() != null ? spot.getCity().getCityName() : null
            );
        }
    }
}
