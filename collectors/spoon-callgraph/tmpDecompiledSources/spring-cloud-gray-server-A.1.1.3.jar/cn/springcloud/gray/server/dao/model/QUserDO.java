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

import cn.springcloud.gray.server.dao.model.UserDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QUserDO
extends EntityPathBase<UserDO> {
    private static final long serialVersionUID = 1664480192L;
    public static final QUserDO userDO = new QUserDO("userDO");
    public final StringPath account = this.createString("account");
    public final DateTimePath<Date> createTime = this.createDateTime("createTime", Date.class);
    public final StringPath name = this.createString("name");
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath password = this.createString("password");
    public final StringPath roles = this.createString("roles");
    public final NumberPath<Integer> status = this.createNumber("status", Integer.class);
    public final StringPath userId = this.createString("userId");

    public QUserDO(String variable) {
        super(UserDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QUserDO(Path<? extends UserDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserDO(PathMetadata metadata) {
        super(UserDO.class, metadata);
    }
}

