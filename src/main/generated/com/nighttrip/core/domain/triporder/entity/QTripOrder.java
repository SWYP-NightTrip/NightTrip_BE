package com.nighttrip.core.domain.triporder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTripOrder is a Querydsl query type for TripOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTripOrder extends EntityPathBase<TripOrder> {

    private static final long serialVersionUID = 1215407932L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTripOrder tripOrder = new QTripOrder("tripOrder");

    public final StringPath arrivalTime = createString("arrivalTime");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.nighttrip.core.global.enums.ItemType> itemType = createEnum("itemType", com.nighttrip.core.global.enums.ItemType.class);

    public final com.nighttrip.core.domain.memo.entity.QMemo memo;

    public final NumberPath<Long> orderIndex = createNumber("orderIndex", Long.class);

    public final com.nighttrip.core.domain.touristspot.entity.QTouristSpot touristSpot;

    public final com.nighttrip.core.domain.tripday.entity.QTripDay tripDay;

    public QTripOrder(String variable) {
        this(TripOrder.class, forVariable(variable), INITS);
    }

    public QTripOrder(Path<? extends TripOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTripOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTripOrder(PathMetadata metadata, PathInits inits) {
        this(TripOrder.class, metadata, inits);
    }

    public QTripOrder(Class<? extends TripOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.memo = inits.isInitialized("memo") ? new com.nighttrip.core.domain.memo.entity.QMemo(forProperty("memo"), inits.get("memo")) : null;
        this.touristSpot = inits.isInitialized("touristSpot") ? new com.nighttrip.core.domain.touristspot.entity.QTouristSpot(forProperty("touristSpot"), inits.get("touristSpot")) : null;
        this.tripDay = inits.isInitialized("tripDay") ? new com.nighttrip.core.domain.tripday.entity.QTripDay(forProperty("tripDay"), inits.get("tripDay")) : null;
    }

}

