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

import cn.springcloud.gray.server.dao.model.GrayDecisionDO;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Date;

public class QGrayDecisionDO
extends EntityPathBase<GrayDecisionDO> {
    private static final long serialVersionUID = -1738951596L;
    public static final QGrayDecisionDO grayDecisionDO = new QGrayDecisionDO("grayDecisionDO");
    public final NumberPath<Long> id = this.createNumber("id", Long.class);
    public final StringPath infos = this.createString("infos");
    public final StringPath instanceId = this.createString("instanceId");
    public final StringPath name = this.createString("name");
    public final DateTimePath<Date> operateTime = this.createDateTime("operateTime", Date.class);
    public final StringPath operator = this.createString("operator");
    public final NumberPath<Long> policyId = this.createNumber("policyId", Long.class);

    public QGrayDecisionDO(String variable) {
        super(GrayDecisionDO.class, PathMetadataFactory.forVariable((String)variable));
    }

    public QGrayDecisionDO(Path<? extends GrayDecisionDO> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrayDecisionDO(PathMetadata metadata) {
        super(GrayDecisionDO.class, metadata);
    }
}

