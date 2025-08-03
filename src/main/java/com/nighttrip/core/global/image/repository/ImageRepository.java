package com.nighttrip.core.global.image.repository;

import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageUrl, Long> {
    List<ImageUrl> findByImageTypeAndRelatedId(ImageType imageType, Long relatedId);
}

