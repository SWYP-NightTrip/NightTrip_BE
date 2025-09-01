package com.nighttrip.core.ai.repository;

import com.nighttrip.core.ai.dto.CandidateDto;
import com.nighttrip.core.ai.dto.SpotRowDto;
import com.nighttrip.core.domain.touristspot.entity.TouristSpot;
import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TouristSpotRepositoryAi extends JpaRepository<TouristSpot, Long> {

    // === 1) 후보 조회: 서비스 시그니처 그대로 ===
    default List<CandidateDto> findCandidates(String cityId, int limit, int offset) {
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
                    ts.subWeight,
                    ts.checkCount,
                    ts.computedMeta,
                    ts.latitude,
                    ts.longitude
                )
                from TouristSpot ts
                where ts.city.cityName = :cityName
                order by coalesce(ts.mainWeight, 0) desc, ts.id asc
            """)
    List<CandidateDto> findCandidatesPage(@Param("cityName") String cityName, Pageable pageable);

    // 서브쿼리 ORDER BY 제거 + 단일값 보장(가장 작은 URL을 대표로)
    @Query("""
            select new com.nighttrip.core.ai.dto.SpotRowDto(
                ts.id,
                ts.spotName,
                ts.address,
                ts.category,
                coalesce(count(tsr), 0),
                coalesce(avg(tsr.scope), 0.0),
                (
                  select min(iu.url)
                  from ImageUrl iu
                  where iu.relatedId = ts.id
                    and iu.imageType = :imageType
                    and iu.imageSizeType = :sizeType
                )
            )
            from TouristSpot ts
            left join TouristSpotReview tsr on tsr.touristSpot = ts
            where ts.id in :ids
            group by ts.id, ts.spotName, ts.address, ts.category
            """)
    List<SpotRowDto> findRowsWithThumbByIds(
            @Param("ids") Collection<Long> ids,
            @Param("imageType") ImageType imageType,
            @Param("sizeType") ImageSizeType sizeType
    );
}
