package com.nighttrip.core.domain.touristspot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTouristSpotReview is a Querydsl query type for TouristSpotReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTouristSpotReview extends EntityPathBase<TouristSpotReview> {

    private static final long serialVersionUID = 1351692564L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTouristSpotReview touristSpotReview = new QTouristSpotReview("touristSpotReview");

    public final StringPath content = createString("content");

    public final NumberPath<Long> reviewId = createNumber("reviewId", Long.class);

    public final NumberPath<Integer> scope = createNumber("scope", Integer.class);

    public final StringPath thumbnailUri = createString("thumbnailUri");

    public final QTouristSpot touristSpot;

    public final com.nighttrip.core.domain.user.entity.QUser user;

    public QTouristSpotReview(String variable) {
        this(TouristSpotReview.class, forVariable(variable), INITS);
    }

    public QTouristSpotReview(Path<? extends TouristSpotReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTouristSpotReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTouristSpotReview(PathMetadata metadata, PathInits inits) {
        this(TouristSpotReview.class, metadata, inits);
    }

    public QTouristSpotReview(Class<? extends TouristSpotReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.touristSpot = inits.isInitialized("touristSpot") ? new QTouristSpot(forProperty("touristSpot"), inits.get("touristSpot")) : null;
        this.user = inits.isInitialized("user") ? new com.nighttrip.core.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

