package com.nighttrip.core.domain.avatar.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAvatar is a Querydsl query type for Avatar
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAvatar extends EntityPathBase<Avatar> {

    private static final long serialVersionUID = -516885176L;

    public static final QAvatar avatar = new QAvatar("avatar");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath uri = createString("uri");

    public QAvatar(String variable) {
        super(Avatar.class, forVariable(variable));
    }

    public QAvatar(Path<? extends Avatar> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAvatar(PathMetadata metadata) {
        super(Avatar.class, metadata);
    }

}

