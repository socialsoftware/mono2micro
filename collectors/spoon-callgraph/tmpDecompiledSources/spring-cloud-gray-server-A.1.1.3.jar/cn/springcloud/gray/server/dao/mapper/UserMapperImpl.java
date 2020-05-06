/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.UserMapper;
import cn.springcloud.gray.server.dao.model.UserDO;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl
implements UserMapper {
    @Override
    public List<UserDO> models2dos(Iterable<UserInfo> d) {
        if (d == null) {
            return null;
        }
        ArrayList<UserDO> list = new ArrayList<UserDO>();
        for (UserInfo userInfo : d) {
            list.add(this.model2do(userInfo));
        }
        return list;
    }

    @Override
    public List<UserInfo> dos2models(Iterable<UserDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<UserInfo> list = new ArrayList<UserInfo>();
        for (UserDO userDO : d) {
            list.add(this.do2model(userDO));
        }
        return list;
    }

    @Override
    public UserDO model2do(UserInfo d) {
        if (d == null) {
            return null;
        }
        UserDO userDO_ = new UserDO();
        if (d.getUserId() != null) {
            userDO_.setUserId(d.getUserId());
        }
        if (d.getName() != null) {
            userDO_.setName(d.getName());
        }
        if (d.getAccount() != null) {
            userDO_.setAccount(d.getAccount());
        }
        userDO_.setStatus(d.getStatus());
        if (d.getOperator() != null) {
            userDO_.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            userDO_.setOperateTime(d.getOperateTime());
        }
        userDO_.setRoles(this.ary2str(d.getRoles()));
        return userDO_;
    }

    @Override
    public UserInfo do2model(UserDO d) {
        if (d == null) {
            return null;
        }
        UserInfo userInfo_ = new UserInfo();
        if (d.getUserId() != null) {
            userInfo_.setUserId(d.getUserId());
        }
        if (d.getName() != null) {
            userInfo_.setName(d.getName());
        }
        if (d.getAccount() != null) {
            userInfo_.setAccount(d.getAccount());
        }
        if (d.getStatus() != null) {
            userInfo_.setStatus(d.getStatus());
        }
        if (d.getOperator() != null) {
            userInfo_.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            userInfo_.setOperateTime(d.getOperateTime());
        }
        userInfo_.setRoles(this.str2ary(d.getRoles()));
        return userInfo_;
    }

    @Override
    public void do2model(UserDO d, UserInfo m) {
        if (d == null) {
            return;
        }
        if (d.getUserId() != null) {
            m.setUserId(d.getUserId());
        }
        if (d.getName() != null) {
            m.setName(d.getName());
        }
        if (d.getAccount() != null) {
            m.setAccount(d.getAccount());
        }
        if (d.getStatus() != null) {
            m.setStatus(d.getStatus());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
        m.setRoles(this.str2ary(d.getRoles()));
    }

    @Override
    public void model2do(UserInfo m, UserDO d) {
        if (m == null) {
            return;
        }
        if (m.getUserId() != null) {
            d.setUserId(m.getUserId());
        }
        if (m.getName() != null) {
            d.setName(m.getName());
        }
        if (m.getAccount() != null) {
            d.setAccount(m.getAccount());
        }
        d.setStatus(m.getStatus());
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
        d.setRoles(this.ary2str(m.getRoles()));
    }
}

