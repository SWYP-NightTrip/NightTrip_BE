package com.nighttrip.core.ai.service;

import com.nighttrip.core.ai.repository.TouristSpotRepositoryAi;
import com.nighttrip.core.domain.touristspot.dto.SpotDetailsDto;
import com.nighttrip.core.domain.touristspot.dto.TouristSpotDetailResponse;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.global.enums.ErrorCode;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, value = "aiTransactionManager") // ★ AI 전용 Tx
public class AiTouristSpotReadService {

    private final TouristSpotRepositoryAi aiRepo;

    @Qualifier("aiJdbcTemplate") // AiDataSourceConfig 에서 등록
    private final JdbcTemplate aiJdbc;

    public TouristSpotDetailResponse getDetail(Long spotId) {
        // 1) 기본 엔티티(단일 row) — 연관/컬렉션 접근 금지!
        TouristSpot ts = aiRepo.findById(spotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        // 2) 리뷰 집계 (ai_base.tourist_spot_review)
        Double avg = qObj(
                "select coalesce(avg(scope), 0) from ai_base.tourist_spot_review where tourist_spot_id = ?",
                Double.class, 0.0, spotId
        );
        Long cnt = qObj(
                "select count(*) from ai_base.tourist_spot_review where tourist_spot_id = ?",
                Long.class, 0L, spotId
        );

        // 3) 이미지 (ai_base.image_url) — 존재하는 컬럼으로만!
        List<String> images = qList(
                """
                select coalesce(ncp_image_url, url) as u
                  from ai_base.image_url
                 where image_type = ?
                   and image_size_type = ?
                   and related_id = ?
                 order by image_url_id asc
                """,
                ImageType.TOURIST_SPOT.name(), ImageSizeType.DETAIL.name(), spotId
        );

        // 4) 해시태그 (ai_base.tourist_spot_hashtags)
        List<String> hashTags = qList(
                """
                select hashtag
                  from ai_base.tourist_spot_hashtags
                 where tourist_spot_id = ?
                 order by hashtag asc
                """,
                spotId
        );

        // 5) 상세 디테일 — ai_base에 tourist_spot_details 가 없으니 빈 리스트
        List<SpotDetailsDto> spotDetails = List.of();

        // 6) 좋아요 — AI 전용에서는 계산 생략
        boolean isLiked = false;

        // 7) 최종 DTO
        return TouristSpotDetailResponse.fromEntity(
                ts,
                avg == null ? 0.0 : avg,
                cnt == null ? 0L : cnt,
                isLiked,
                images,
                hashTags,
                spotDetails
        );
    }

    // ===== helper =====
    private <T> T qObj(String sql, Class<T> type, T def, Object... args) {
        try {
            T v = aiJdbc.queryForObject(sql, type, args);
            return v == null ? def : v;
        } catch (DataAccessException e) {
            return def; // 테이블/컬럼 미존재 시 안전하게 기본값
        }
    }

    private List<String> qList(String sql, Object... args) {
        try {
            return aiJdbc.query(sql, (rs, i) -> rs.getString(1), args);
        } catch (DataAccessException e) {
            return List.of();
        }
    }
}
