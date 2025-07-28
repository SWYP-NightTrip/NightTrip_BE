package com.nighttrip.core.global.service;

import com.nighttrip.core.global.dto.SearchDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";

    private void incrementPopularKeywords(List<SearchDocument> documents) {
        if (documents != null && !documents.isEmpty()) {
            documents.stream()
                    .map(SearchDocument::getName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .map(String::trim) // 앞뒤 공백 제거
                    .distinct()
                    .forEach(name -> {
                        redisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORDS_KEY, name, 1);
                    });
        }
    }

    /**
     * 자동 완성/추천 검색어 제안
     * 검색 결과로 반환된 SearchDocument의 name 필드를 Redis에 집계합니다.
     * @param searchText 사용자가 입력한 검색어
     * @return 검색 제안 문서 목록
     */
    public List<SearchDocument> suggestAutoComplete(String searchText) {
        Query matchQuery = QueryBuilders.match(m -> m
                .field("suggestName")
                .query(searchText)
                .analyzer("korean_autocomplete_analyzer")
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(matchQuery)
                .withMaxResults(10)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);
        List<SearchDocument> searchResults = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        incrementPopularKeywords(searchResults);

        return searchResults;
    }

    /**
     * 메인 검색 기능
     * 검색 결과로 반환된 SearchDocument의 name 필드를 Redis에 집계합니다.
     * @param query 사용자가 입력한 검색어
     * @return 검색된 문서 목록
     */
    public List<SearchDocument> search(String query) {
        Query multiMatchQuery = QueryBuilders.multiMatch(m -> m
                .fields("name", "cityName", "description")
                .query(query)
                .fuzziness("AUTO")
                .operator(Operator.And)
                .analyzer("korean_search_analyzer")
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(multiMatchQuery)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);
        List<SearchDocument> searchResults = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());


        incrementPopularKeywords(searchResults);

        return searchResults;
    }

    /**
     * Redis에서 인기 검색어 목록을 조회합니다.
     * @param limit 조회할 인기 검색어의 개수
     * @return 인기 검색어 문자열 리스트
     */
    public List<String> getPopularKeywords(int limit) {
        Set<Object> keywords = redisTemplate.opsForZSet().reverseRange(POPULAR_KEYWORDS_KEY, 0, limit - 1);
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * ★★★ 하드코딩된 추천 검색어 목록을 반환합니다. ★★★
     * (사용자 유입이 적은 서비스 초반에 활용)
     * @return 추천 검색어 문자열 리스트 (최대 10개)
     */
    public List<String> getRecommendedKeywordsHardcoded() {
        return Arrays.asList(
                "제주도 여행",
                "서울 맛집",
                "강릉 카페",
                "부산 해운대",
                "가족 여행 추천",
                "혼자 떠나는 여행",
                "경주 한옥마을",
                "속초 가볼만한곳",
                "여수 밤바다",
                "춘천 닭갈비"
        );
    }
}