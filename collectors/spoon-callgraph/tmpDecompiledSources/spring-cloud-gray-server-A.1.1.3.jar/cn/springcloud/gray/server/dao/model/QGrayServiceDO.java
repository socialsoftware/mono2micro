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

import cn.springcloud.gray.server.dao.model.GrayServiceDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QGrayServiceDO
extends EntityPathBase<GrayServiceDO> {
    private static final long serialVersionUID = 1886151027L;
    public static final QGrayServiceDO grayServiceDO = new QGrayServiceDO("grayServiceDO");
    public final StringPath contextPath = this.createString("contextPath");
    public final StringPath describe = this.createString("describe");
    public final NumberPath<Integer> grayInstanceNumber = this.createNumber("grayInstanceNumber", Integer.class);
    public final NumberPath<Integer> instanceNumber = this.createNumber("instanceNumber", Integer.class);
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath serviceId = this.createString("serviceId");
    public final StringPath serviceName = this.createString("serviceName");

    public QGrayServiceDO(String variable) {
        super(GrayServiceDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QGrayServiceDO(Path<? extends GrayServiceDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrayServiceDO(PathMetadata metadata) {
        super(GrayServiceDO.class, metadata);
    }
}

