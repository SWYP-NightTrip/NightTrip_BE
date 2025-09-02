package com.nighttrip.core.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookMarkFolder is a Querydsl query type for BookMarkFolder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookMarkFolder extends EntityPathBase<BookMarkFolder> {

    private static final long serialVersionUID = 2102411653L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookMarkFolder bookMarkFolder = new QBookMarkFolder("bookMarkFolder");

    public final ListPath<BookMark, QBookMark> bookMarks = this.<BookMark, QBookMark>createList("bookMarks", BookMark.class, QBookMark.class, PathInits.DIRECT2);

    public final StringPath folderName = createString("folderName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public QBookMarkFolder(String variable) {
        this(BookMarkFolder.class, forVariable(variable), INITS);
    }

    public QBookMarkFolder(Path<? extends BookMarkFolder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookMarkFolder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookMarkFolder(PathMetadata metadata, PathInits inits) {
        this(BookMarkFolder.class, metadata, inits);
    }

    public QBookMarkFolder(Class<? extends BookMarkFolder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

