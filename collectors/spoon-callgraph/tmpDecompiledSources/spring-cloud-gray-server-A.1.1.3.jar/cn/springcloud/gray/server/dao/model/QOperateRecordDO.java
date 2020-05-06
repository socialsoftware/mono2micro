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

import cn.springcloud.gray.server.dao.model.OperateRecordDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QOperateRecordDO
extends EntityPathBase<OperateRecordDO> {
    private static final long serialVersionUID = 350148438L;
    public static final QOperateRecordDO operateRecordDO = new QOperateRecordDO("operateRecordDO");
    public final StringPath apiResCode = this.createString("apiResCode");
    public final StringPath handler = this.createString("handler");
    public final StringPath headlerArgs = this.createString("headlerArgs");
    public final StringPath httpMethod = this.createString("httpMethod");
    public final NumberPath<Long> id = this.createNumber("id", Long.class);
    public final StringPath ip = this.createString("ip");
    public final NumberPath<Integer> operateState = this.createNumber("operateState", Integer.class);
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final StringPath queryString = this.createString("queryString");
    public final StringPath uri = this.createString("uri");

    public QOperateRecordDO(String variable) {
        super(OperateRecordDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QOperateRecordDO(Path<? extends OperateRecordDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOperateRecordDO(PathMetadata metadata) {
        super(OperateRecordDO.class, metadata);
    }
}

