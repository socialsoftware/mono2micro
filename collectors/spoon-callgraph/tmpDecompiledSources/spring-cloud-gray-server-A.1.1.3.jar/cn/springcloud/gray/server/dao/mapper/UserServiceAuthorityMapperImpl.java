/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.UserServiceAuthorityMapper;
import cn.springcloud.gray.server.dao.model.UserServiceAuthorityDO;
import cn.springcloud.gray.server.module.user.domain.UserServiceAuthority;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserServiceAuthorityMapperImpl
implements UserServiceAuthorityMapper {
    @Override
    public UserServiceAuthorityDO model2do(UserServiceAuthority d) {
        if (d == null) {
            return null;
        }
        UserServiceAuthorityDO userServiceAuthorityDO = new UserServiceAuthorityDO();
        if (d.getId() != null) {
            userServiceAuthorityDO.setId(d.getId());
        }
        if (d.getUserId() != null) {
            userServiceAuthorityDO.setUserId(d.getUserId());
        }
        if (d.getServiceId() != null) {
            userServiceAuthorityDO.setServiceId(d.getServiceId());
        }
        if (d.getOperator() != null) {
            userServiceAuthorityDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            userServiceAuthorityDO.setOperateTime(d.getOperateTime());
        }
        return userServiceAuthorityDO;
    }

    @Override
    public List<UserServiceAuthorityDO> models2dos(Iterable<UserServiceAuthority> d) {
        if (d == null) {
            return null;
        }
        ArrayList<UserServiceAuthorityDO> list = new ArrayList<UserServiceAuthorityDO>();
        for (UserServiceAuthority userServiceAuthority : d) {
            list.add(this.model2do(userServiceAuthority));
        }
        return list;
    }

    @Override
    public UserServiceAuthority do2model(UserServiceAuthorityDO d) {
        if (d == null) {
            return null;
        }
        UserServiceAuthority userServiceAuthority = new UserServiceAuthority();
        if (d.getId() != null) {
            userServiceAuthority.setId(d.getId());
        }
        if (d.getUserId() != null) {
            userServiceAuthority.setUserId(d.getUserId());
        }
        if (d.getServiceId() != null) {
            userServiceAuthority.setServiceId(d.getServiceId());
        }
        if (d.getOperator() != null) {
            userServiceAuthority.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            userServiceAuthority.setOperateTime(d.getOperateTime());
        }
        return userServiceAuthority;
    }

    @Override
    public List<UserServiceAuthority> dos2models(Iterable<UserServiceAuthorityDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<UserServiceAuthority> list = new ArrayList<UserServiceAuthority>();
        for (UserServiceAuthorityDO userServiceAuthorityDO : d) {
            list.add(this.do2model(userServiceAuthorityDO));
        }
        return list;
    }

    @Override
    public void do2model(UserServiceAuthorityDO d, UserServiceAuthority m) {
        if (d == null) {
            return;
        }
        if (d.getId() != null) {
            m.setId(d.getId());
        }
        if (d.getUserId() != null) {
            m.setUserId(d.getUserId());
        }
        if (d.getServiceId() != null) {
            m.setServiceId(d.getServiceId());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
    }

    @Override
    public void model2do(UserServiceAuthority m, UserServiceAuthorityDO d) {
        if (m == null) {
            return;
        }
        if (m.getId() != null) {
            d.setId(m.getId());
        }
        if (m.getUserId() != null) {
            d.setUserId(m.getUserId());
        }
        if (m.getServiceId() != null) {
            d.setServiceId(m.getServiceId());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
    }
}

