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

import cn.springcloud.gray.server.dao.model.GrayTrackDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QGrayTrackDO
extends EntityPathBase<GrayTrackDO> {
    private static final long serialVersionUID = 1142587209L;
    public static final QGrayTrackDO grayTrackDO = new QGrayTrackDO("grayTrackDO");
    public final NumberPath<Long> id = this.createNumber("id", Long.class);
    public final StringPath infos = this.createString("infos");
    public final StringPath instanceId = this.createString("instanceId");
    public final StringPath name = this.createString("name");
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath serviceId = this.createString("serviceId");

    public QGrayTrackDO(String variable) {
        super(GrayTrackDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QGrayTrackDO(Path<? extends GrayTrackDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrayTrackDO(PathMetadata metadata) {
        super(GrayTrackDO.class, metadata);
    }
}

