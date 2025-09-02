package com.nighttrip.core.domain.tripday.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTripDay is a Querydsl query type for TripDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTripDay extends EntityPathBase<TripDay> {

    private static final long serialVersionUID = 1416488060L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTripDay tripDay = new QTripDay("tripDay");

    public final NumberPath<Integer> dayOrder = createNumber("dayOrder", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.nighttrip.core.domain.triporder.entity.TripOrder, com.nighttrip.core.domain.triporder.entity.QTripOrder> tripOrders = this.<com.nighttrip.core.domain.triporder.entity.TripOrder, com.nighttrip.core.domain.triporder.entity.QTripOrder>createList("tripOrders", com.nighttrip.core.domain.triporder.entity.TripOrder.class, com.nighttrip.core.domain.triporder.entity.QTripOrder.class, PathInits.DIRECT2);

    public final com.nighttrip.core.domain.tripplan.entity.QTripPlan tripPlan;

    public QTripDay(String variable) {
        this(TripDay.class, forVariable(variable), INITS);
    }

    public QTripDay(Path<? extends TripDay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTripDay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTripDay(PathMetadata metadata, PathInits inits) {
        this(TripDay.class, metadata, inits);
    }

    public QTripDay(Class<? extends TripDay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tripPlan = inits.isInitialized("tripPlan") ? new com.nighttrip.core.domain.tripplan.entity.QTripPlan(forProperty("tripPlan"), inits.get("tripPlan")) : null;
    }

}

