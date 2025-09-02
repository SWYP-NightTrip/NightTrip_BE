package com.nighttrip.core.domain.touristspot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTourLike is a Querydsl query type for TourLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTourLike extends EntityPathBase<TourLike> {

    private static final long serialVersionUID = -1623463993L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTourLike tourLike = new QTourLike("tourLike");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> likedAt = createDateTime("likedAt", java.time.LocalDateTime.class);

    public final QTouristSpot touristSpot;

    public final com.nighttrip.core.domain.user.entity.QUser user;

    public QTourLike(String variable) {
        this(TourLike.class, forVariable(variable), INITS);
    }

    public QTourLike(Path<? extends TourLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTourLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTourLike(PathMetadata metadata, PathInits inits) {
        this(TourLike.class, metadata, inits);
    }

    public QTourLike(Class<? extends TourLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.touristSpot = inits.isInitialized("touristSpot") ? new QTouristSpot(forProperty("touristSpot"), inits.get("touristSpot")) : null;
        this.user = inits.isInitialized("user") ? new com.nighttrip.core.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

