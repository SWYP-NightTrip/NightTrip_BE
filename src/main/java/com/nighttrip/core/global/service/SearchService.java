package com.nighttrip.core.global.service;

import com.nighttrip.core.global.dto.RecommendedKeyword;
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.util.LocationFormatter;
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
                    .map(String::trim)
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

        if (searchText == null || searchText.trim().isEmpty()) {
            return List.of();
        }

        Query matchQuery = QueryBuilders.match(m -> m
                .field("name.autocomplete")
                .query(searchText)
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(matchQuery)
                .withMaxResults(10)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * 메인 검색 기능
     * 검색 결과로 반환된 SearchDocument의 name 필드를 Redis에 집계합니다.
     * @param query 사용자가 입력한 검색어
     * @return 검색된 문서 목록
     */
    public List<SearchDocument> search(String query) {
        Query multiMatchQuery = QueryBuilders.multiMatch(m -> m
                .fields(
                        "name^4",
                        "category^2",
                        "cityName^2",
                        "address^2",
                        "description",
                        "name.autocomplete"
                )
                .query(query)
                .fuzziness("AUTO")
                .operator(Operator.Or)
                .minimumShouldMatch("70%")
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(multiMatchQuery)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, SearchDocument.class);

        List<SearchDocument> searchResults = searchHits.getSearchHits().stream()
                .map(hit -> {
                    SearchDocument doc = hit.getContent();

                    String formattedCityName = LocationFormatter.formatForSearch(doc.getCityName());

                    return SearchDocument.builder()
                            .id(doc.getId())
                            .type(doc.getType())
                            .name(doc.getName())
                            .description(doc.getDescription())
                            .cityName(formattedCityName)
                            .address(doc.getAddress())
                            .category(doc.getCategory())
                            .imageUrl(doc.getImageUrl())
                            .build();
                })
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

    public List<RecommendedKeyword> getRecommendedKeywordsHardcoded() {
        return Arrays.asList(
                new RecommendedKeyword("tourist_spot_81616", "월미도"),
                new RecommendedKeyword("tourist_spot_86762", "타임스퀘어"),
                new RecommendedKeyword("tourist_spot_92498", "소노캄 거제"),
                new RecommendedKeyword("tourist_spot_80289", "오창호수공원"),
                new RecommendedKeyword("tourist_spot_58160", "동문재래시장"),
                new RecommendedKeyword("tourist_spot_57181", "대천해수욕장"),
                new RecommendedKeyword("tourist_spot_79709", "영일대해수욕장"),
                new RecommendedKeyword("tourist_spot_82738", "일산해수욕장"),
                new RecommendedKeyword("tourist_spot_73699", "삽교호관광지"),
                new RecommendedKeyword("tourist_spot_83524", "전주월드컵경기장")
        );
    }


    public List<RecommendedKeyword> getRecommendedKeywordsHardcoded2() {
        return Arrays.asList(
                new RecommendedKeyword("tourist_spot_65807", "대평시장"),
                new RecommendedKeyword("tourist_spot_75139", "세종호수공원"),
                new RecommendedKeyword("tourist_spot_93467", "스타필드 하남"),
                new RecommendedKeyword("tourist_spot_93823", "신세계백화점 대구신세계점"),
                new RecommendedKeyword("tourist_spot_46884", "광주기아챔피언스필드"),
                new RecommendedKeyword("tourist_spot_73455", "삼길포항"),
                new RecommendedKeyword("tourist_spot_73723", "상남시장"),
                new RecommendedKeyword("tourist_spot_75509", "속초해변"),
                new RecommendedKeyword("tourist_spot_75770", "송정해수욕장"),
                new RecommendedKeyword("tourist_spot_75968", "수성못")
        );
    }

}