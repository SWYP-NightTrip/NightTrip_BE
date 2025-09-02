package com.nighttrip.core.domain.tripday.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCityOnTripDay is a Querydsl query type for CityOnTripDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCityOnTripDay extends EntityPathBase<CityOnTripDay> {

    private static final long serialVersionUID = -469005134L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCityOnTripDay cityOnTripDay = new QCityOnTripDay("cityOnTripDay");

    public final com.nighttrip.core.domain.city.entity.QCity city;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.nighttrip.core.domain.tripplan.entity.QTripPlan tripPlan;

    public QCityOnTripDay(String variable) {
        this(CityOnTripDay.class, forVariable(variable), INITS);
    }

    public QCityOnTripDay(Path<? extends CityOnTripDay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCityOnTripDay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCityOnTripDay(PathMetadata metadata, PathInits inits) {
        this(CityOnTripDay.class, metadata, inits);
    }

    public QCityOnTripDay(Class<? extends CityOnTripDay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.city = inits.isInitialized("city") ? new com.nighttrip.core.domain.city.entity.QCity(forProperty("city")) : null;
        this.tripPlan = inits.isInitialized("tripPlan") ? new com.nighttrip.core.domain.tripplan.entity.QTripPlan(forProperty("tripPlan"), inits.get("tripPlan")) : null;
    }

}

