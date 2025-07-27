package com.nighttrip.core.global.image;

import com.nighttrip.core.global.enums.ItemType;
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
    private ImageType itemType;

    private Long relatedId;

    private String url;

    private boolean isMain;
}
