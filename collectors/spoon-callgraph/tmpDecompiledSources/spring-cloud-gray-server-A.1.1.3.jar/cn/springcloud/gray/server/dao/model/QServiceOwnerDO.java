/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  com.querydsl.core.types.Path
 *  com.querydsl.core.types.PathMetadata
 *  com.querydsl.core.types.PathMetadataFactory
 *  com.querydsl.core.types.dsl.DateTimePath
 *  com.querydsl.core.types.dsl.EntityPathBase
 *  com.querydsl.core.types.dsl.StringPath
 */
package cn.springcloud.gray.server.dao.model;

import cn.springcloud.gray.server.dao.model.ServiceOwnerDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QServiceOwnerDO
extends EntityPathBase<ServiceOwnerDO> {
    private static final long serialVersionUID = 1941955123L;
    public static final QServiceOwnerDO serviceOwnerDO = new QServiceOwnerDO("serviceOwnerDO");
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath serviceId = this.createString("serviceId");
    public final StringPath userId = this.createString("userId");

    public QServiceOwnerDO(String variable) {
        super(ServiceOwnerDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QServiceOwnerDO(Path<? extends ServiceOwnerDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QServiceOwnerDO(PathMetadata metadata) {
        super(ServiceOwnerDO.class, metadata);
    }
}

