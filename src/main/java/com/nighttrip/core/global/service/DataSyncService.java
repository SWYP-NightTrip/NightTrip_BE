package com.nighttrip.core.global.service;

import co.elastic.clients.elasticsearch.indices.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.city.repository.CityRepository;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.domain.touristspot.repository.TouristSpotRepository;
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageSizeType;
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
        try {

            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(INDEX_NAME));
            if (elasticsearchClient.indices().exists(existsRequest).value()) {
                DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(d -> d.index(INDEX_NAME));
                elasticsearchClient.indices().delete(deleteRequest);
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
            System.out.println("인덱스 생성 완료");

            List<City> cities = cityRepository.findAll();
            if (!cities.isEmpty()) {
                List<SearchDocument> cityDocuments = cities.stream()
                        .map(city -> {
                            Set<String> suggestions = new HashSet<>();
                            String fullCityName = city.getCityName();
                            suggestions.add(fullCityName);
                            String[] words = fullCityName.split(" ");
                            for (String word : words) {
                                if (!word.isEmpty()) { suggestions.add(word); }
                            }
                            for (int i = 0; i < words.length; i++) {
                                StringBuilder sb = new StringBuilder();
                                for (int j = i; j < words.length; j++) {
                                    if (j > i) { sb.append(" "); }
                                    sb.append(words[j]);
                                    suggestions.add(sb.toString());
                                }
                            }
                            if (fullCityName.contains("특별시")) { suggestions.add(fullCityName.substring(0, fullCityName.indexOf("특별시"))); }
                            if (fullCityName.contains("광역시")) { suggestions.add(fullCityName.substring(0, fullCityName.indexOf("광역시"))); }
                            if (fullCityName.contains("도")) { suggestions.add(fullCityName.substring(0, fullCityName.indexOf("도"))); }
                            if (fullCityName.endsWith("구")) { suggestions.add(fullCityName.replace("구", "")); }
                            if (fullCityName.endsWith("군")) { suggestions.add(fullCityName.replace("군", "")); }
                            if (fullCityName.endsWith("시")) { suggestions.add(fullCityName.replace("시", "")); }
                            suggestions.add(fullCityName.replace(" ", ""));
                            if (words.length >= 2) { suggestions.add(words[0] + words[1]); suggestions.add(words[1] + words[0]); }
                            if (fullCityName.startsWith("서울특별시")) { suggestions.add("서울특"); }

                            String image = imageRepository.findTHUMBNAILImage(String.valueOf(ImageType.CITY), city.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            return SearchDocument.builder()
                                    .id("city_" + city.getId())
                                    .type("city")
                                    .name(fullCityName)
                                    .imageUrl(image)
                                    .suggestName(new ArrayList<>(suggestions))
                                    .build();
                        })
                        .collect(Collectors.toList());
                searchDocumentRepository.saveAll(cityDocuments);
                System.out.println(cityDocuments.size() + "개의 City 문서 색인 완료.");
            } else {
                System.out.println("동기화할 City 데이터가 없습니다.");
            }


            List<TouristSpot> touristSpots = touristSpotRepository.findAll();
            System.out.println("PostgreSQL에서 총 " + touristSpots.size() + "개의 TouristSpot 데이터를 가져왔습니다.");
            if (!touristSpots.isEmpty()) {
                List<SearchDocument> touristSpotDocuments = touristSpots.stream()
                        .map(touristSpot -> {
                            String  mainImageUrl = imageRepository.findSEARCHImage(String.valueOf(ImageType.TOURIST_SPOT), touristSpot.getId())
                                    .map(ImageUrl::getUrl)
                                    .orElse(null);

                            Set<String> suggestions = new HashSet<>();
                            String spotName = touristSpot.getSpotName();
                            suggestions.add(spotName);

                            String[] nameWords = spotName.split(" ");
                            for (String word : nameWords) {
                                if (!word.isEmpty()) suggestions.add(word);
                            }
                            for (int i = 0; i < nameWords.length; i++) {
                                StringBuilder sb = new StringBuilder();
                                for (int j = i; j < nameWords.length; j++) {
                                    if (j > i) sb.append(" ");
                                    sb.append(nameWords[j]);
                                    suggestions.add(sb.toString());
                                }
                            }

                            if (nameWords.length >= 2) {
                                for (int i = 0; i < nameWords.length; i++) {
                                    for (int j = 0; j < nameWords.length; j++) {
                                        if (i != j) {
                                            suggestions.add(nameWords[i] + " " + nameWords[j]);
                                            suggestions.add(nameWords[i] + nameWords[j]);
                                        }
                                    }
                                }
                            }


                            String compactName = spotName.replace(" ", "");
                            suggestions.add(compactName);

                            int len = compactName.length();
                            for (int i = 0; i < len; i++) {
                                for (int j = i + 2; j <= len; j++) {
                                    suggestions.add(compactName.substring(i, j));
                                }
                            }
                            return SearchDocument.builder()
                                    .id("tourist_spot_" + touristSpot.getId())
                                    .type("tourist_spot")
                                    .name(touristSpot.getSpotName())
                                    .description(touristSpot.getSpotDescription())
                                    .cityName(touristSpot.getCity() != null ? touristSpot.getCity().getCityName() : null)
                                    .category(touristSpot.getCategory().getKoreanName())
                                    .imageUrl(mainImageUrl)
                                    .suggestName(new ArrayList<>(suggestions))
                                    .build();
                        })
                        .collect(Collectors.toList());
                searchDocumentRepository.saveAll(touristSpotDocuments);
                System.out.println(touristSpotDocuments.size() + "개의 TouristSpot 문서 색인 완료.");
            } else {
                System.out.println("동기화할 TouristSpot 데이터가 없습니다.");
            }

            RefreshRequest refreshRequest = RefreshRequest.of(r -> r.index(INDEX_NAME));
            elasticsearchClient.indices().refresh(refreshRequest);
            System.out.println("Elasticsearch 인덱스 '" + INDEX_NAME + "' 리프레시 완료.");

        } catch (Exception e) {
            System.err.println("!!! Elasticsearch 초기 동기화 중 치명적인 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== Elasticsearch 초기 동기화 종료 ===");
    }

    /**
     * 리소스 파일의 내용을 읽어 JSON Map으로 파싱하는 헬퍼 메서드.
     */
    private Map<String, Object> readResourceFileAsMap(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String jsonString = new String(data, StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonString, Map.class);
    }
}
