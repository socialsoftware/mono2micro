/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package cn.springcloud.gray.server.service;

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.io.Serializable;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstraceCRUDService<MODEL, REPOSITORY extends JpaRepository<T, ID>, T, ID extends Serializable> {
    protected abstract REPOSITORY getRepository();

    protected abstract ModelMapper<MODEL, T> getModelMapper();

    protected T save(T entity) {
        return (T)this.getRepository().save(entity);
    }

    @Transactional
    public MODEL saveModel(MODEL entity) {
        T t = this.model2do(entity);
        return this.do2model(this.save(t));
    }

    protected Iterable<T> save(Iterable<T> entities) {
        return this.getRepository().save(entities);
    }

    public List<MODEL> saveModels(Iterable<MODEL> entities) {
        return this.dos2models(this.save((Iterable<T>)this.models2dos(entities)));
    }

    protected T findOne(ID id) {
        return (T)this.getRepository().findOne(id);
    }

    public MODEL findOneModel(ID id) {
        return this.do2model(this.findOne(id));
    }

    public boolean exists(ID id) {
        return this.getRepository().exists(id);
    }

    protected Iterable<T> findAll() {
        return this.getRepository().findAll();
    }

    protected Iterable<T> findAll(Iterable<ID> ids) {
        return this.getRepository().findAll(ids);
    }

    public List<MODEL> findAllModel() {
        return this.dos2models(this.findAll());
    }

    public List<MODEL> findAllModel(Iterable<ID> ids) {
        return this.dos2models(this.findAll(ids));
    }

    public Page<MODEL> findAllModels(Pageable pageable) {
        return PaginationUtils.convert(pageable, this.getRepository().findAll(pageable), this.getModelMapper());
    }

    public long count() {
        return this.getRepository().count();
    }

    public void delete(ID id) {
        this.getRepository().delete(id);
    }

    protected void delete(Iterable<? extends T> entities) {
        this.getRepository().delete(entities);
    }

    public void deleteModel(Iterable<MODEL> models) {
        this.delete((Iterable<? extends T>)this.models2dos(models));
    }

    public void deleteAll() {
        this.getRepository().deleteAll();
    }

    protected List<MODEL> dos2models(Iterable<T> dos) {
        return this.getModelMapper().dos2models(dos);
    }

    protected List<T> models2dos(Iterable<MODEL> models) {
        return this.getModelMapper().models2dos(models);
    }

    protected MODEL do2model(T t) {
        return this.getModelMapper().do2model(t);
    }

    protected T model2do(MODEL model) {
        return this.getModelMapper().model2do(model);
    }

    protected void do2model(T t, MODEL model) {
        this.getModelMapper().do2model(t, model);
    }

    protected void model2do(MODEL model, T t) {
        this.getModelMapper().model2do(model, t);
    }
}

