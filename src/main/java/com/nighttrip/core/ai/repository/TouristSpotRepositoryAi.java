package com.nighttrip.core.ai.repository;

import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.ai.dto.CandidateDto;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TouristSpotRepositoryAi extends JpaRepository<TouristSpot, Long> {

    // === 1) 후보 조회: 서비스 시그니처 그대로 ===
    default List<CandidateDto> findCandidates(Long cityId, int limit, int offset) {
        int size = Math.max(1, limit);
        int page = Math.max(0, offset / size);
        return findCandidatesPage(cityId, PageRequest.of(page, size));
    }

    @Query("""
        select new com.nighttrip.core.ai.dto.CandidateDto(
            ts.id,
            ts.spotName,
            ts.category,
            ts.mainWeight,
            ts.checkCount,
            ts.computedMeta
        )
        from TouristSpot ts
        where ts.city.id = :cityId
        order by coalesce(ts.mainWeight, 0) desc, ts.id asc
    """)
    List<CandidateDto> findCandidatesPage(@Param("cityId") Long cityId, Pageable pageable);

    // 서브쿼리 ORDER BY 제거 + 단일값 보장(가장 작은 URL을 대표로)
    @Query("""
        select
            ts.id,
            ts.spotName,
            ts.address,
            ts.category,
            ts.spotDescription,
            (
              select min(iu.url)
              from ImageUrl iu
              where iu.relatedId = ts.id
                and iu.imageType = :imageType
                and iu.imageSizeType = :sizeType
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
