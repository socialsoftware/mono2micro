/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  com.querydsl.core.types.Path
 *  com.querydsl.core.types.PathMetadata
 *  com.querydsl.core.types.PathMetadataFactory
 *  com.querydsl.core.types.dsl.DateTimePath
 *  com.querydsl.core.types.dsl.EntityPathBase
 *  com.querydsl.core.types.dsl.NumberPath
 *  com.querydsl.core.types.dsl.StringPath
 */
package cn.springcloud.gray.server.dao.model;

import cn.springcloud.gray.server.dao.model.UserServiceAuthorityDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QUserServiceAuthorityDO
extends EntityPathBase<UserServiceAuthorityDO> {
    private static final long serialVersionUID = -540674450L;
    public static final QUserServiceAuthorityDO userServiceAuthorityDO = new QUserServiceAuthorityDO("userServiceAuthorityDO");
    public final NumberPath<Long> id = this.createNumber("id", Long.class);
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath serviceId = this.createString("serviceId");
    public final StringPath userId = this.createString("userId");

    public QUserServiceAuthorityDO(String variable) {
        super(UserServiceAuthorityDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QUserServiceAuthorityDO(Path<? extends UserServiceAuthorityDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserServiceAuthorityDO(PathMetadata metadata) {
        super(UserServiceAuthorityDO.class, metadata);
    }
}

