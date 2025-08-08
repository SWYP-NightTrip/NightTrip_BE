package com.nighttrip.core.global.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();

            if (exists) {
                System.out.println("기존 인덱스 '" + INDEX_NAME + "' 삭제 시도...");
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX_NAME)));
                System.out.println("삭제 요청 완료. 인덱스가 완전히 삭제될 때까지 대기...");
                for (int i = 0; i < 10; i++) {
                    boolean stillExists = elasticsearchClient.indices()
                            .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                            .value();
                    if (!stillExists) {
                        System.out.println("인덱스 삭제 완료.");
                        break;
                    }
                    Thread.sleep(500);  // 500ms 대기
                }
            } else {
                System.out.println("삭제할 인덱스가 존재하지 않음.");
            }


            Map<String, Object> settingsMap = readResourceFileAsMap(SETTINGS_PATH);
            Map<String, Object> mappingsMap = readResourceFileAsMap(MAPPINGS_PATH);
            Map<String, Object> settingsOnly = (Map<String, Object>) settingsMap.get("index");

            Map<String, Object> fullIndexRequest = new HashMap<>();
            fullIndexRequest.put("settings", settingsOnly);
            fullIndexRequest.put("mappings", mappingsMap);

            String fullJson = objectMapper.writeValueAsString(fullIndexRequest);
            InputStream fullInputStream = new ByteArrayInputStream(fullJson.getBytes(StandardCharsets.UTF_8));

            try {
                elasticsearchClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .withJson(fullInputStream)
                );
                System.out.println("인덱스 생성 완료.");
            } catch (ElasticsearchException e) {
                if (e.getMessage().contains("resource_already_exists_exception")) {
                    System.out.println("인덱스가 이미 존재함. 무시하고 진행.");
                } else {
                    throw e;
                }
            }


            List<City> cities = cityRepository.findAll();
            if (!cities.isEmpty()) {
                List<SearchDocument> cityDocuments = cities.stream()
                        .map(city -> {
                            Set<String> suggestions = generateSuggestions(city.getCityName());

                            String image = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.CITY), city.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return SearchDocument.builder()
                                    .id("city_" + city.getId())
                                    .type("city")
                                    .name(city.getCityName())
                                    .imageUrl(image)
                                    .suggestName(new ArrayList<>(suggestions))
                                    .build();
                        }).collect(Collectors.toList());

                searchDocumentRepository.saveAll(cityDocuments);
                System.out.println(cityDocuments.size() + "개의 City 문서 색인 완료.");
            }

            List<TouristSpot> touristSpots = touristSpotRepository.findAll();
            System.out.println("총 " + touristSpots.size() + "개의 TouristSpot 가져옴.");
            if (!touristSpots.isEmpty()) {
                List<SearchDocument> spotDocs = touristSpots.stream()
                        .map(spot -> {
                            Set<String> suggestions = new HashSet<>();
                            suggestions.add(spot.getSpotName());

                            if (spot.getCity() != null) {
                                suggestions.addAll(generateSuggestions(spot.getCity().getCityName()));
                            }

                            String image = imageRepository.findSEARCHImage(String.valueOf(ImageType.TOURIST_SPOT), spot.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return SearchDocument.builder()
                                    .id("tourist_spot_" + spot.getId())
                                    .type("tourist_spot")
                                    .name(spot.getSpotName())
                                    .description(spot.getSpotDescription())
                                    .cityName(spot.getCity() != null ? spot.getCity().getCityName() : null)
                                    .category(spot.getCategory().getKoreanName())
                                    .imageUrl(image)
                                    .suggestName(new ArrayList<>(suggestions))
                                    .build();
                        }).collect(Collectors.toList());

                searchDocumentRepository.saveAll(spotDocs);
                System.out.println(spotDocs.size() + "개의 TouristSpot 문서 색인 완료.");
            }

            elasticsearchClient.indices().refresh(RefreshRequest.of(r -> r.index(INDEX_NAME)));
            System.out.println("Elasticsearch 인덱스 리프레시 완료.");

        } catch (Exception e) {
            System.err.println("!!! Elasticsearch 초기 동기화 중 오류: " + e.getMessage());
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

    private Set<String> generateSuggestions(String fullCityName) {
        Set<String> suggestions = new HashSet<>();
        suggestions.add(fullCityName);
        String[] words = fullCityName.split(" ");
        for (String word : words) {
            if (!word.isEmpty()) suggestions.add(word);
        }
        for (int i = 0; i < words.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < words.length; j++) {
                if (j > i) sb.append(" ");
                sb.append(words[j]);
                suggestions.add(sb.toString());
            }
        }
        if (fullCityName.contains("특별시")) suggestions.add(fullCityName.substring(0, fullCityName.indexOf("특별시")));
        if (fullCityName.contains("광역시")) suggestions.add(fullCityName.substring(0, fullCityName.indexOf("광역시")));
        if (fullCityName.contains("도")) suggestions.add(fullCityName.substring(0, fullCityName.indexOf("도")));
        if (fullCityName.endsWith("구")) suggestions.add(fullCityName.replace("구", ""));
        if (fullCityName.endsWith("군")) suggestions.add(fullCityName.replace("군", ""));
        if (fullCityName.endsWith("시")) suggestions.add(fullCityName.replace("시", ""));
        suggestions.add(fullCityName.replace(" ", ""));
        if (words.length >= 2) {
            suggestions.add(words[0] + words[1]);
            suggestions.add(words[1] + words[0]);
        }
        if (fullCityName.startsWith("서울특별시")) suggestions.add("서울특");

        return suggestions;
    }
}
