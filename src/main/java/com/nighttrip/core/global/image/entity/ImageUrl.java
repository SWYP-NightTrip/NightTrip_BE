package com.nighttrip.core.global.image.entity;

import com.nighttrip.core.global.enums.ImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image_url")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_url_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    private Long relatedId;

    private String url;

    private boolean isMain;

    public ImageUrl(ImageType imageType, Long relatedId, String url, boolean isMain) {
        this.imageType = imageType;
        this.relatedId = relatedId;
        this.url = url;
        this.isMain = isMain;
    }
}
