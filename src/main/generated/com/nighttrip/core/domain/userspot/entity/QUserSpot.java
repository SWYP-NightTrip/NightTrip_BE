package com.nighttrip.core.domain.userspot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSpot is a Querydsl query type for UserSpot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSpot extends EntityPathBase<UserSpot> {

    private static final long serialVersionUID = 434631504L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSpot userSpot = new QUserSpot("userSpot");

    public final StringPath address = createString("address");

    public final EnumPath<com.nighttrip.core.global.enums.SpotCategory> category = createEnum("category", com.nighttrip.core.global.enums.SpotCategory.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath spotMemo = createString("spotMemo");

    public final StringPath spotName = createString("spotName");

    public final SetPath<com.nighttrip.core.global.enums.SpotDetails, EnumPath<com.nighttrip.core.global.enums.SpotDetails>> touristSpotDetails = this.<com.nighttrip.core.global.enums.SpotDetails, EnumPath<com.nighttrip.core.global.enums.SpotDetails>>createSet("touristSpotDetails", com.nighttrip.core.global.enums.SpotDetails.class, EnumPath.class, PathInits.DIRECT2);

    public final com.nighttrip.core.domain.user.entity.QUser user;

    public QUserSpot(String variable) {
        this(UserSpot.class, forVariable(variable), INITS);
    }

    public QUserSpot(Path<? extends UserSpot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSpot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSpot(PathMetadata metadata, PathInits inits) {
        this(UserSpot.class, metadata, inits);
    }

    public QUserSpot(Class<? extends UserSpot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.nighttrip.core.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

