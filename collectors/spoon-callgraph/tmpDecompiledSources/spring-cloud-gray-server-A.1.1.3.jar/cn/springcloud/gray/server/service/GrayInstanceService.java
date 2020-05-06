/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.domain.Specification
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Service
 */
package cn.springcloud.gray.server.service;

import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.dao.mapper.GrayInstanceMapper;
import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.GrayInstanceDO;
import cn.springcloud.gray.server.dao.repository.GrayInstanceRepository;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.service.GrayPolicyService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrayInstanceService
extends AbstraceCRUDService<GrayInstance, GrayInstanceRepository, GrayInstanceDO, String> {
    @Autowired
    private GrayInstanceRepository repository;
    @Autowired
    private GrayPolicyService grayPolicyService;
    @Autowired
    private GrayInstanceMapper grayInstanceMapper;

    @Override
    protected GrayInstanceRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<GrayInstance, GrayInstanceDO> getModelMapper() {
        return this.grayInstanceMapper;
    }

    public List<GrayInstance> findByServiceId(String serviceId) {
        return this.grayInstanceMapper.dos2models(this.repository.findByServiceId(serviceId));
    }

    @Transactional
    public void deleteByServiceId(String serviceId) {
        this.findByServiceId(serviceId).forEach(entity -> {
            this.delete(entity.getInstanceId());
            this.grayPolicyService.deleteByInstanceId(entity.getInstanceId());
        });
    }

    @Transactional
    public void deleteReactById(String id) {
        this.delete(id);
        this.grayPolicyService.findByInstanceId(id).forEach(entity -> this.grayPolicyService.deleteReactById(entity.getId()));
    }

    @Override
    public GrayInstance saveModel(GrayInstance grayInstance) {
        grayInstance.setLastUpdateDate(new Date());
        return super.saveModel(grayInstance);
    }

    public List<GrayInstance> findAllByStatus(GrayStatus grayStatus, Collection<InstanceStatus> instanceStatusList) {
        String[] instanceStatusAry = this.toArray(instanceStatusList);
        return this.grayInstanceMapper.dos2models(this.repository.findAllByGrayStatusAndInstanceStatusIn(grayStatus.name(), instanceStatusAry));
    }

    public Page<GrayInstance> listGrayInstancesByServiceId(String serviceId, Pageable pageable) {
        Page<GrayInstanceDO> entities = this.repository.findAllByServiceId(serviceId, pageable);
        return PaginationUtils.convert(pageable, entities, this.grayInstanceMapper);
    }

    public List<GrayInstance> findByServiceId(String serviceId, Collection<InstanceStatus> instanceStatusList) {
        String[] instanceStatusAry = this.toArray(instanceStatusList);
        return this.dos2models(this.repository.findAllByServiceIdAndInstanceStatusIn(serviceId, instanceStatusAry));
    }

    public List<GrayInstance> listGrayInstancesByNormalInstanceStatus(Collection<InstanceStatus> instanceStatusList) {
        String[] instanceStatusAry = this.toArray(instanceStatusList);
        Specification spec = (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();
            Predicate predGrayStatus = cb.equal(root.get("grayStatus").as(String.class), GrayStatus.OPEN.name());
            predicates.add(predGrayStatus);
            CriteriaBuilder.In<String> predInstanceStatusIn = cb.in(root.get("instanceStatus").as(String.class));
            for (String instanceStatus : instanceStatusAry) {
                predInstanceStatusIn.value(instanceStatus);
            }
            Predicate predTrayLock = cb.equal(root.get("grayLock").as(Integer.class), 1);
            predicates.add(cb.or(predInstanceStatusIn, (Expression<Boolean>)predTrayLock));
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return query.getRestriction();
        };
        return this.dos2models(this.repository.findAll(spec));
    }

    private String[] toArray(Collection<InstanceStatus> instanceStatusList) {
        return instanceStatusList.stream().map(Enum::name).collect(Collectors.toList()).toArray(new String[instanceStatusList.size()]);
    }

    public List<GrayInstance> findAllByEvictableRecords(int lastUpdateDateExpireDays, Collection<InstanceStatus> evictionInstanceStatus) {
        Date lastUpdateDate = Date.from(LocalDateTime.now().minusDays(lastUpdateDateExpireDays).atZone(ZoneId.systemDefault()).toInstant());
        String[] instanceStatusAry = this.toArray(evictionInstanceStatus);
        return this.dos2models(this.repository.findAllByLastUpdateDateBeforeAndInstanceStatusIn(lastUpdateDate, instanceStatusAry));
    }
}

