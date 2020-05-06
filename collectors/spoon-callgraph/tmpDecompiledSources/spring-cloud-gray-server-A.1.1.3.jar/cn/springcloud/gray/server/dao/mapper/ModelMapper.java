/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.mapstruct.MappingTarget
 */
package cn.springcloud.gray.server.dao.mapper;

import java.util.List;
import org.mapstruct.MappingTarget;

public interface ModelMapper<MO, DO> {
    public DO model2do(MO var1);

    public List<DO> models2dos(Iterable<MO> var1);

    public MO do2model(DO var1);

    public List<MO> dos2models(Iterable<DO> var1);

    public void do2model(DO var1, @MappingTarget MO var2);

    public void model2do(MO var1, @MappingTarget DO var2);
}

