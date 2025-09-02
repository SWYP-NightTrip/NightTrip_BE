package com.nighttrip.core.global.service;

import co.elastic.clients.elasticsearch.indices.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.repository.SearchDocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


import co.elastic.clients.elasticsearch.ElasticsearchClient;

@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final CityRepository cityRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ImageRepository imageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INDEX_NAME = "search_documents";
    private static final String SETTINGS_PATH = "elasticsearch/settings.json";
    private static final String MAPPINGS_PATH = "elasticsearch/mappings.json";
    @PostConstruct
    @Transactional(readOnly = true)
    public void initialElasticsearchSync() {
        System.out.println("=== Elasticsearch 초기 동기화 시작 ===");
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value();


            if (exists) {
                elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
                System.out.println("🗑 기존 인덱스 삭제 완료: " + INDEX_NAME);
            }


            Map<String, Object> settingsMap = readResourceFileAsMap(SETTINGS_PATH);
            Map<String, Object> mappingsMap = readResourceFileAsMap(MAPPINGS_PATH);

            Map<String, Object> settingsOnly = (Map<String, Object>) settingsMap.get("index");

            Map<String, Object> fullIndexRequest = new HashMap<>();
            fullIndexRequest.put("settings", settingsOnly);
            fullIndexRequest.put("mappings", mappingsMap);

            String fullJson = objectMapper.writeValueAsString(fullIndexRequest);
            InputStream fullInputStream = new ByteArrayInputStream(fullJson.getBytes(StandardCharsets.UTF_8));

            elasticsearchClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .withJson(fullInputStream)
            );

            System.out.println("🆕 인덱스 생성 완료: " + INDEX_NAME);

            List<City> cities = cityRepository.findAll();
            if (!cities.isEmpty()) {
                List<SearchDocument> cityDocuments = cities.stream()
                        .map(city -> {
                            //Set<String> suggestions = generateCitySuggestions(city.getCityName());

                            String image = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.CITY), city.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return SearchDocument.builder()
                                    .id("city_" + city.getId())
                                    .type("city")
                                    .name(city.getCityName())
                                    .address(null)
                                    .imageUrl(image)
                                    .build();
                        })
                        .collect(Collectors.toList());

                searchDocumentRepository.saveAll(cityDocuments);
                System.out.println("📌 " + cityDocuments.size() + "개의 City 문서 색인 완료.");
            } else {
                System.out.println("⚠️ 동기화할 City 데이터가 없습니다.");
            }

            // 4. TouristSpot 데이터 색인
            List<TouristSpot> touristSpots = touristSpotRepository.findAll();
            System.out.println("📥 PostgreSQL에서 총 " + touristSpots.size() + "개의 TouristSpot 데이터를 가져왔습니다.");

            if (!touristSpots.isEmpty()) {
                List<SearchDocument> touristSpotDocuments = touristSpots.stream()
                        .map(touristSpot -> {
                            String mainImageUrl = imageRepository.findSEARCHImage(String.valueOf(ImageType.TOURIST_SPOT), touristSpot.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            //Set<String> suggestions = generateTouristSpotSuggestions(touristSpot.getSpotName());

                            return SearchDocument.builder()
                                    .id("tourist_spot_" + touristSpot.getId())
                                    .type("tourist_spot")
                                    .name(touristSpot.getSpotName())
                                    .description(touristSpot.getSpotDescription())
                                    .cityName(touristSpot.getCity() != null ? touristSpot.getCity().getCityName() : null)
                                    .address(touristSpot.getAddress())
                                    .category(touristSpot.getCategory().getKoreanName())
                                    .imageUrl(mainImageUrl)
                                    .build();
                        })
                        .collect(Collectors.toList());

                searchDocumentRepository.saveAll(touristSpotDocuments);
                System.out.println("📌 " + touristSpotDocuments.size() + "개의 TouristSpot 문서 색인 완료.");
            } else {
                System.out.println("⚠️ 동기화할 TouristSpot 데이터가 없습니다.");
            }

            // 5. 인덱스 리프레시
            elasticsearchClient.indices().refresh(RefreshRequest.of(r -> r.index(INDEX_NAME)));
            System.out.println("🔄 Elasticsearch 인덱스 '" + INDEX_NAME + "' 리프레시 완료.");

        } catch (Exception e) {
            System.err.println("❌ Elasticsearch 초기 동기화 중 치명적인 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== Elasticsearch 초기 동기화 종료 ===");
    }


    private Map<String, Object> readResourceFileAsMap(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String jsonString = new String(data, StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonString, Map.class);
    }
    private String readResourceFile(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("File not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
