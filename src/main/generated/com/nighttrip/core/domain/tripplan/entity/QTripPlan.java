package com.nighttrip.core.domain.tripplan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTripPlan is a Querydsl query type for TripPlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTripPlan extends EntityPathBase<TripPlan> {

    private static final long serialVersionUID = -1392276398L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTripPlan tripPlan = new QTripPlan("tripPlan");

    public final ListPath<com.nighttrip.core.domain.tripday.entity.CityOnTripDay, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay> cityOnTripDays = this.<com.nighttrip.core.domain.tripday.entity.CityOnTripDay, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay>createList("cityOnTripDays", com.nighttrip.core.domain.tripday.entity.CityOnTripDay.class, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> createdAt = createDate("createdAt", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.nighttrip.core.global.enums.TripFeature> isFeatured = createEnum("isFeatured", com.nighttrip.core.global.enums.TripFeature.class);

    public final BooleanPath isShared = createBoolean("isShared");

    public final NumberPath<Long> numIndex = createNumber("numIndex", Long.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final EnumPath<com.nighttrip.core.global.enums.TripStatus> status = createEnum("status", com.nighttrip.core.global.enums.TripStatus.class);

    public final StringPath title = createString("title");

    public final ListPath<com.nighttrip.core.domain.tripday.entity.TripDay, com.nighttrip.core.domain.tripday.entity.QTripDay> tripDays = this.<com.nighttrip.core.domain.tripday.entity.TripDay, com.nighttrip.core.domain.tripday.entity.QTripDay>createList("tripDays", com.nighttrip.core.domain.tripday.entity.TripDay.class, com.nighttrip.core.domain.tripday.entity.QTripDay.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> updatedAt = createDate("updatedAt", java.time.LocalDate.class);

    public final com.nighttrip.core.domain.user.entity.QUser user;

    public QTripPlan(String variable) {
        this(TripPlan.class, forVariable(variable), INITS);
    }

    public QTripPlan(Path<? extends TripPlan> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTripPlan(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTripPlan(PathMetadata metadata, PathInits inits) {
        this(TripPlan.class, metadata, inits);
    }

    public QTripPlan(Class<? extends TripPlan> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.nighttrip.core.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

