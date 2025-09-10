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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, value = "aiTransactionManager") // AI 전용 Tx
public class AiTouristSpotReadService {

    private final TouristSpotRepositoryAi aiRepo;

    @Qualifier("aiJdbcTemplate")           // AiDataSourceConfig 에서 만든 JDBC 템플릿
    private final JdbcTemplate aiJdbc;

    public TouristSpotDetailResponse getDetail(Long spotId) {
        // 1) 기본 엔티티 (AI DB)
        TouristSpot ts = aiRepo.findById(spotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOURIST_SPOT_NOT_FOUND));

        // 2) 리뷰 집계 (AI DB, 없으면 0 처리)
        Double avg = aiJdbc.queryForObject(
                "select coalesce(avg(scope), 0) from tourist_spot_review where tourist_spot_id = ?",
                Double.class, spotId
        );
        Long cnt = aiJdbc.queryForObject(
                "select count(*) from tourist_spot_review where tourist_spot_id = ?",
                Long.class, spotId
        );

        // 3) 이미지 URL (AI DB) — 테이블/컬럼명은 실제 스키마에 맞게 필요 시 조정
        List<String> images = aiJdbc.query(
                """
                select coalesce(ncp_image_url, url) as u
                  from ai_base.image_url
                 where image_type = ?
                   and image_size_type = ?
                   and related_id = ?
                 order by image_url_id asc
                """,
                (rs, rowNum) -> rs.getString("u"),
                ImageType.TOURIST_SPOT.name(),
                ImageSizeType.DETAIL.name(),
                spotId
        );

        // 4) 상세 태그(디테일) — 엔티티 관계 활용 (LAZY여도 @Transactional(readOnly=true) 범위)
        List<SpotDetailsDto> spotDetails = ts.getTouristSpotDetails() == null
                ? List.of()
                : ts.getTouristSpotDetails().stream()
                .map(d -> new SpotDetailsDto(d.getTypeKey(), d.getKoreanName()))
                .toList();

        // 5) 해시태그 — 엔티티 파생 메서드 그대로 사용
        List<String> hashTags = ts.getHashTagsAsList() == null ? List.of() : ts.getHashTagsAsList();

        // 6) 좋아요 — AI 전용은 계산 생략(false) 또는 AI DB에 like 테이블 있으면 별도 exists 처리
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
}
