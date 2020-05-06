/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.digest.DigestUtils
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.domain.Specification
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Service
 */
package cn.springcloud.gray.server.service;

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.mapper.UserMapper;
import cn.springcloud.gray.server.dao.model.UserDO;
import cn.springcloud.gray.server.dao.repository.UserRepository;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import cn.springcloud.gray.server.module.user.domain.UserQuery;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService
extends AbstraceCRUDService<UserInfo, UserRepository, UserDO, String> {
    private static final String PASSWORD_SLAT = "!@#DFS3df";
    @Autowired
    private UserRepository repository;
    @Autowired
    private UserMapper userMapper;

    @Override
    protected UserRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<UserInfo, UserDO> getModelMapper() {
        return this.userMapper;
    }

    public Page<UserInfo> query(final UserQuery userQuery, Pageable pageable) {
        Specification<UserDO> specification = new Specification<UserDO>(){

            public Predicate toPredicate(Root<UserDO> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                ArrayList<Predicate> predicates = new ArrayList<Predicate>();
                if (userQuery.getStatus() != -1) {
                    predicates.add(cb.equal(root.get("status").as(Integer.class), userQuery.getStatus()));
                }
                ArrayList orPredicates = new ArrayList();
                if (StringUtils.isNotEmpty(userQuery.getKey())) {
                    predicates.add(cb.or((Expression<Boolean>)cb.like(root.get("account").as(String.class), "%" + userQuery.getKey() + "%"), (Expression<Boolean>)cb.like(root.get("name").as(String.class), "%" + userQuery.getKey() + "%")));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
        Page doPage = this.repository.findAll((Specification)specification, pageable);
        return PaginationUtils.convert(pageable, doPage, this.getModelMapper());
    }

    public void resetPassword(String userId, String password) {
        UserDO userDO = (UserDO)this.findOne(userId);
        if (userDO == null) {
            return;
        }
        userDO.setPassword(this.markPassword(password));
        this.save(userDO);
    }

    private String markPassword(String password) {
        return DigestUtils.md5Hex((String)(password + PASSWORD_SLAT));
    }

    public UserDO findDOByAccount(String account) {
        return this.repository.findByAccount(account);
    }

    public UserInfo login(String account, String password) {
        UserDO userDO = this.findDOByAccount(account);
        if (userDO == null || Objects.equals(userDO.getStatus(), 0)) {
            return null;
        }
        if (!StringUtils.equals(this.markPassword(password), userDO.getPassword())) {
            return null;
        }
        return (UserInfo)this.do2model(userDO);
    }

    public UserInfo register(UserInfo userInfo, String password) {
        if (this.findDOByAccount(userInfo.getAccount()) != null) {
            return null;
        }
        UserDO userDO = (UserDO)this.model2do(userInfo);
        userDO.setUserId(userDO.getAccount());
        userDO.setPassword(this.markPassword(password));
        userDO.setCreateTime(new Date());
        userDO.setOperateTime(userDO.getCreateTime());
        return (UserInfo)this.do2model(this.save(userDO));
    }

    public void updateUserStatus(String userId, int statusDisabled) {
        UserDO userDO = (UserDO)this.findOne(userId);
        if (userDO == null) {
            return;
        }
        userDO.setStatus(statusDisabled);
        this.save(userDO);
    }

    public boolean resetPassword(String userId, String oldPassword, String newPassword) {
        UserDO userDO = (UserDO)this.findOne(userId);
        if (userDO == null) {
            return false;
        }
        if (!StringUtils.equals(this.markPassword(oldPassword), userDO.getPassword())) {
            return false;
        }
        userDO.setPassword(this.markPassword(newPassword));
        this.save(userDO);
        return true;
    }

    public UserInfo updateUserInfo(UserInfo userInfo) {
        UserDO userDO = (UserDO)this.findOne(userInfo.getUserId());
        if (userDO == null) {
            return null;
        }
        this.model2do(userInfo, userDO);
        userDO.setOperateTime(new Date());
        return (UserInfo)this.do2model(this.save(userDO));
    }

}

