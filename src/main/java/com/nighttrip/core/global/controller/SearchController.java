package com.nighttrip.core.global.controller;

import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.dto.RecommendedKeyword;
import com.nighttrip.core.global.dto.SearchDocument;
import com.nighttrip.core.global.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 자동 완성(서제스트) 기능을 제공합니다.
     * 예시: GET /api/v1/search/autocomplete?query=서울
     *
     * @param query 사용자가 입력한 검색어
     * @return 자동 완성 추천 단어 목록
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<SearchDocument>>> getAutoCompleteSuggestions(@RequestParam String query) {
        List<SearchDocument> suggestions = searchService.suggestAutoComplete(query);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    /**
     * 실제 검색 기능을 제공합니다.
     * 예시: GET /api/v1/search?query=서울여행
     *
     * @param query 사용자가 입력한 검색어
     * @return 검색 결과 문서 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchDocument>>> searchDocuments(@RequestParam String query) {
        List<SearchDocument> results = searchService.search(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<RecommendedKeyword>>> getPopularSearchKeywords() {
        List<RecommendedKeyword> recommendedKeywords = searchService.getRecommendedKeywordsHardcoded2();
        return ResponseEntity.ok(ApiResponse.success(recommendedKeywords));
    }

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<RecommendedKeyword>>> getRecommendedSearchKeywords() {
        List<RecommendedKeyword> recommendedKeywords = searchService.getRecommendedKeywordsHardcoded();
        return ResponseEntity.ok(ApiResponse.success(recommendedKeywords));
    }

}
