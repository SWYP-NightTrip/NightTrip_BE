package com.nighttrip.core.global.ai.repository;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.global.ai.dto.CandidateDto;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TouristSpotRepositoryAi extends JpaRepository<TouristSpot, Long> {

    // === 1) 후보 조회: 서비스 시그니처 그대로 ===
    default List<CandidateDto> findCandidates(Long cityId, int limit, int offset) {
        int page = (limit > 0) ? (offset / limit) : 0;
        return findCandidatesPage(cityId, PageRequest.of(page, limit));
    }

    // 내부 구현은 Pageable을 쓰는 JPQL (DTO 생성자로 매핑)
    @Query("""
     SELECT new com.nighttrip.core.global.ai.dto.CandidateDto(
        ts.id,
        ts.spotName,
        ts.category,
        ts.mainWeight,
        ts.checkCount,
        ts.computedMeta
     )
     FROM TouristSpot ts
     WHERE ts.city.id = :cityId
     ORDER BY COALESCE(ts.mainWeight,0) DESC, ts.id ASC
  """)
    List<CandidateDto> findCandidatesPage(@Param("cityId") Long cityId, Pageable pageable);


    // === 2) 썸네일 포함 요약: 시그니처 그대로 ===
    @Query("""
      select
         ts.id,
         ts.spotName,
         ts.address,
         ts.category,
         ts.spotDescription,
         (
           select iu.url
             from ImageUrl iu
            where iu.relatedId = ts.id
              and iu.imageType = :imageType
              and iu.imageSizeType = :sizeType
            order by iu.url asc
         )
      from TouristSpot ts
      where ts.id in :ids
  """)
    List<Object[]> findRowsWithThumbByIds(
            @Param("ids") Collection<Long> ids,
            @Param("imageType") ImageType imageType,
            @Param("sizeType") ImageSizeType sizeType
    );
}
