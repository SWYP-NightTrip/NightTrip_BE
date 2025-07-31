package com.nighttrip.core.domain.touristspot.service;

import com.nighttrip.core.global.exception.CityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.Implementation.TouristSpotService;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotPopularityDto;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotResponseDto;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nighttrip.core.domain.touristspot.entity.TouristSpotImageUri;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristSpotServiceImpl implements TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;
    private final CityRepository cityRepository;


    private TouristSpotResponseDto mapToTouristSpotResponseDto(TouristSpot spot) {
        String mainImageUrl = spot.getTouristSpotImageUris().stream()
                .filter(TouristSpotImageUri::isMain)
                .map(TouristSpotImageUri::getUri)
                .findFirst()
                .orElse(null);
        return new TouristSpotResponseDto(
                spot.getId(),
                spot.getSpotName(),
                spot.getAddress(),
                spot.getCategory().getKoreanName(),
                spot.getSpotDescription(),
                mainImageUrl
        );
    }

    @Override
    public List<TouristSpotResponseDto> getPopularTouristSpotsInCity(Long cityId) {
        cityRepository.findById(cityId)
                .orElseThrow(CityNotFoundException::new);

        Pageable pageable = PageRequest.of(0, 7);
        List<TouristSpot> popularSpots = touristSpotRepository.findPopularTouristSpotsByCityId(cityId, pageable);

        return popularSpots.stream()
                .map(this::mapToTouristSpotResponseDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<TouristSpotResponseDto> getRecommendedTouristSpotsInCity(Long cityId) {
        cityRepository.findById(cityId)
                .orElseThrow(CityNotFoundException::new);

        Pageable pageable = PageRequest.of(0, 7);
        List<TouristSpot> recommendedSpots = touristSpotRepository.findRecommendedTouristSpotsByCityId(cityId, pageable);

        return recommendedSpots.stream()
                .map(this::mapToTouristSpotResponseDto)
                .collect(Collectors.toList());
    }
}