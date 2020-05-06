/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.mapstruct.Mapper
 *  org.mapstruct.Mapping
 *  org.mapstruct.MappingTarget
 *  org.mapstruct.NullValueCheckStrategy
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.UserDO;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel="spring", nullValueCheckStrategy=NullValueCheckStrategy.ALWAYS)
public interface UserMapper
extends ModelMapper<UserInfo, UserDO> {
    @Mapping(target="roles", expression="java(ary2str(d.getRoles()))")
    @Override
    public UserDO model2do(UserInfo var1);

    @Mapping(target="roles", expression="java(str2ary(d.getRoles()))")
    @Override
    public UserInfo do2model(UserDO var1);

    @Mapping(target="roles", expression="java(str2ary(d.getRoles()))")
    @Override
    public void do2model(UserDO var1, @MappingTarget UserInfo var2);

    @Mapping(target="roles", expression="java(ary2str(m.getRoles()))")
    @Override
    public void model2do(UserInfo var1, @MappingTarget UserDO var2);

    default public String[] str2ary(String str) {
        return StringUtils.split(str, ',');
    }

    default public String ary2str(String[] ary) {
        return StringUtils.join((Object[])ary, ',');
    }
}

