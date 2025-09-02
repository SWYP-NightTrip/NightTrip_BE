package com.nighttrip.core.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1795462868L;

    public static final QUser user = new QUser("user");

    public final NumberPath<Integer> avartarLevel = createNumber("avartarLevel", Integer.class);

    public final ListPath<BookMarkFolder, QBookMarkFolder> bookMarkFolders = this.<BookMarkFolder, QBookMarkFolder>createList("bookMarkFolders", BookMarkFolder.class, QBookMarkFolder.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final ListPath<com.nighttrip.core.domain.tripplan.entity.PlanLike, com.nighttrip.core.domain.tripplan.entity.QPlanLike> planLikes = this.<com.nighttrip.core.domain.tripplan.entity.PlanLike, com.nighttrip.core.domain.tripplan.entity.QPlanLike>createList("planLikes", com.nighttrip.core.domain.tripplan.entity.PlanLike.class, com.nighttrip.core.domain.tripplan.entity.QPlanLike.class, PathInits.DIRECT2);

    public final NumberPath<Integer> point = createNumber("point", Integer.class);

    public final EnumPath<com.nighttrip.core.global.enums.OauthProvider> provider = createEnum("provider", com.nighttrip.core.global.enums.OauthProvider.class);

    public final EnumPath<com.nighttrip.core.global.enums.UserRole> role = createEnum("role", com.nighttrip.core.global.enums.UserRole.class);

    public final StringPath socialId = createString("socialId");

    public final ListPath<com.nighttrip.core.domain.touristspot.entity.TouristSpotReview, com.nighttrip.core.domain.touristspot.entity.QTouristSpotReview> touristSpotReviews = this.<com.nighttrip.core.domain.touristspot.entity.TouristSpotReview, com.nighttrip.core.domain.touristspot.entity.QTouristSpotReview>createList("touristSpotReviews", com.nighttrip.core.domain.touristspot.entity.TouristSpotReview.class, com.nighttrip.core.domain.touristspot.entity.QTouristSpotReview.class, PathInits.DIRECT2);

    public final ListPath<com.nighttrip.core.domain.tripplan.entity.TripPlan, com.nighttrip.core.domain.tripplan.entity.QTripPlan> tripPlans = this.<com.nighttrip.core.domain.tripplan.entity.TripPlan, com.nighttrip.core.domain.tripplan.entity.QTripPlan>createList("tripPlans", com.nighttrip.core.domain.tripplan.entity.TripPlan.class, com.nighttrip.core.domain.tripplan.entity.QTripPlan.class, PathInits.DIRECT2);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

