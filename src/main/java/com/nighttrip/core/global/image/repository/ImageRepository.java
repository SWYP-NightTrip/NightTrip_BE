package com.nighttrip.core.global.image.repository;

import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageSizeType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageUrl, Long> {
    List<ImageUrl> findByImageTypeAndRelatedId(ImageType imageType, Long relatedId);

    @Query("SELECT i FROM ImageUrl i WHERE i.imageType = :imageType AND i.relatedId = :relatedId AND i.imageSizeType = :imageSizeType")
    Optional<ImageUrl> findImageSizeByTypeAndRelatedId(@Param("imageType") ImageType imageType, @Param("relatedId") Long relatedId, @Param("imageSizeType") ImageSizeType imageSizeType);

}

