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

import cn.springcloud.gray.server.dao.model.GrayInstanceDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QGrayInstanceDO
extends EntityPathBase<GrayInstanceDO> {
    private static final long serialVersionUID = 967954125L;
    public static final QGrayInstanceDO grayInstanceDO = new QGrayInstanceDO("grayInstanceDO");
    public final StringPath des = this.createString("des");
    public final NumberPath<Integer> grayLock = this.createNumber("grayLock", Integer.class);
    public final StringPath grayStatus = this.createString("grayStatus");
    public final StringPath host = this.createString("host");
    public final StringPath instanceId = this.createString("instanceId");
    public final StringPath instanceStatus = this.createString("instanceStatus");
    public final DateTimePath<Date> lastUpdateDate = this.createDateTime("lastUpdateDate", Date.class);
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final NumberPath<Integer> port = this.createNumber("port", Integer.class);
    public final StringPath serviceId = this.createString("serviceId");

    public QGrayInstanceDO(String variable) {
        super(GrayInstanceDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QGrayInstanceDO(Path<? extends GrayInstanceDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrayInstanceDO(PathMetadata metadata) {
        super(GrayInstanceDO.class, metadata);
    }
}

