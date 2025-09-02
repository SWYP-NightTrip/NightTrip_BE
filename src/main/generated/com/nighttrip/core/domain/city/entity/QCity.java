package com.nighttrip.core.domain.city.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCity is a Querydsl query type for City
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCity extends EntityPathBase<City> {

    private static final long serialVersionUID = -139242388L;

    public static final QCity city = new QCity("city");

    public final NumberPath<Integer> checkCount = createNumber("checkCount", Integer.class);

    public final NumberPath<Double> cityConsum = createNumber("cityConsum", Double.class);

    public final StringPath cityName = createString("cityName");

    public final ListPath<com.nighttrip.core.domain.tripday.entity.CityOnTripDay, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay> cityOnTripDays = this.<com.nighttrip.core.domain.tripday.entity.CityOnTripDay, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay>createList("cityOnTripDays", com.nighttrip.core.domain.tripday.entity.CityOnTripDay.class, com.nighttrip.core.domain.tripday.entity.QCityOnTripDay.class, PathInits.DIRECT2);

    public final NumberPath<Double> cityPepoleVisitied = createNumber("cityPepoleVisitied", Double.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.nighttrip.core.domain.touristspot.entity.TouristSpot, com.nighttrip.core.domain.touristspot.entity.QTouristSpot> touristSpots = this.<com.nighttrip.core.domain.touristspot.entity.TouristSpot, com.nighttrip.core.domain.touristspot.entity.QTouristSpot>createList("touristSpots", com.nighttrip.core.domain.touristspot.entity.TouristSpot.class, com.nighttrip.core.domain.touristspot.entity.QTouristSpot.class, PathInits.DIRECT2);

    public QCity(String variable) {
        super(City.class, forVariable(variable));
    }

    public QCity(Path<? extends City> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCity(PathMetadata metadata) {
        super(City.class, metadata);
    }

}

