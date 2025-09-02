package com.nighttrip.core.domain.tripplan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlanLike is a Querydsl query type for PlanLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlanLike extends EntityPathBase<PlanLike> {

    private static final long serialVersionUID = -1034639932L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlanLike planLike = new QPlanLike("planLike");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> likedAt = createDateTime("likedAt", java.time.LocalDateTime.class);

    public final QTripPlan tripPlan;

    public final com.nighttrip.core.domain.user.entity.QUser user;

    public QPlanLike(String variable) {
        this(PlanLike.class, forVariable(variable), INITS);
    }

    public QPlanLike(Path<? extends PlanLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlanLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlanLike(PathMetadata metadata, PathInits inits) {
        this(PlanLike.class, metadata, inits);
    }

    public QPlanLike(Class<? extends PlanLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tripPlan = inits.isInitialized("tripPlan") ? new QTripPlan(forProperty("tripPlan"), inits.get("tripPlan")) : null;
        this.user = inits.isInitialized("user") ? new com.nighttrip.core.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

