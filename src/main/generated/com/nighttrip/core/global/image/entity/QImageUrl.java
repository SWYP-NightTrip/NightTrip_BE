package com.nighttrip.core.global.image.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QImageUrl is a Querydsl query type for ImageUrl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImageUrl extends EntityPathBase<ImageUrl> {

    private static final long serialVersionUID = 1087904466L;

    public static final QImageUrl imageUrl = new QImageUrl("imageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.nighttrip.core.global.enums.ImageSizeType> imageSizeType = createEnum("imageSizeType", com.nighttrip.core.global.enums.ImageSizeType.class);

    public final EnumPath<com.nighttrip.core.global.enums.ImageType> imageType = createEnum("imageType", com.nighttrip.core.global.enums.ImageType.class);

    public final NumberPath<Long> relatedId = createNumber("relatedId", Long.class);

    public final StringPath url = createString("url");

    public QImageUrl(String variable) {
        super(ImageUrl.class, forVariable(variable));
    }

    public QImageUrl(Path<? extends ImageUrl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QImageUrl(PathMetadata metadata) {
        super(ImageUrl.class, metadata);
    }

}

