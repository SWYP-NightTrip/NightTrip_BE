package com.nighttrip.core.domain.touristspot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTouristSpot is a Querydsl query type for TouristSpot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTouristSpot extends EntityPathBase<TouristSpot> {

    private static final long serialVersionUID = 1272885916L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTouristSpot touristSpot = new QTouristSpot("touristSpot");

    public final StringPath address = createString("address");

    public final ListPath<com.nighttrip.core.domain.user.entity.BookMark, com.nighttrip.core.domain.user.entity.QBookMark> bookMarks = this.<com.nighttrip.core.domain.user.entity.BookMark, com.nighttrip.core.domain.user.entity.QBookMark>createList("bookMarks", com.nighttrip.core.domain.user.entity.BookMark.class, com.nighttrip.core.domain.user.entity.QBookMark.class, PathInits.DIRECT2);

    public final EnumPath<com.nighttrip.core.global.enums.SpotCategory> category = createEnum("category", com.nighttrip.core.global.enums.SpotCategory.class);

    public final NumberPath<Integer> checkCount = createNumber("checkCount", Integer.class);

    public final com.nighttrip.core.domain.city.entity.QCity city;

    public final StringPath computedMeta = createString("computedMeta");

    public final SetPath<String, StringPath> hashTags = this.<String, StringPath>createSet("hashTags", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final StringPath link = createString("link");

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final NumberPath<Integer> mainWeight = createNumber("mainWeight", Integer.class);

    public final DateTimePath<java.sql.Timestamp> metaUpdatedAt = createDateTime("metaUpdatedAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> metaVersion = createNumber("metaVersion", Integer.class);

    public final StringPath spotDescription = createString("spotDescription");

    public final StringPath spotName = createString("spotName");

    public final NumberPath<Integer> subWeight = createNumber("subWeight", Integer.class);

    public final StringPath telephone = createString("telephone");

    public final SetPath<com.nighttrip.core.global.enums.SpotDetails, EnumPath<com.nighttrip.core.global.enums.SpotDetails>> touristSpotDetails = this.<com.nighttrip.core.global.enums.SpotDetails, EnumPath<com.nighttrip.core.global.enums.SpotDetails>>createSet("touristSpotDetails", com.nighttrip.core.global.enums.SpotDetails.class, EnumPath.class, PathInits.DIRECT2);

    public final ListPath<TouristSpotReview, QTouristSpotReview> touristSpotReviews = this.<TouristSpotReview, QTouristSpotReview>createList("touristSpotReviews", TouristSpotReview.class, QTouristSpotReview.class, PathInits.DIRECT2);

    public final ListPath<TourLike, QTourLike> tourLikes = this.<TourLike, QTourLike>createList("tourLikes", TourLike.class, QTourLike.class, PathInits.DIRECT2);

    public QTouristSpot(String variable) {
        this(TouristSpot.class, forVariable(variable), INITS);
    }

    public QTouristSpot(Path<? extends TouristSpot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTouristSpot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTouristSpot(PathMetadata metadata, PathInits inits) {
        this(TouristSpot.class, metadata, inits);
    }

    public QTouristSpot(Class<? extends TouristSpot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.city = inits.isInitialized("city") ? new com.nighttrip.core.domain.city.entity.QCity(forProperty("city")) : null;
    }

}

