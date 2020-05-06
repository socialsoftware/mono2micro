/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GraySourceEventPublisher
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.autoconfigure.domain.EntityScan
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.ComponentScan
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.data.jpa.repository.config.EnableJpaRepositories
 *  org.springframework.transaction.annotation.EnableTransactionManagement
 *  org.springframework.transaction.annotation.Transactional
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.event.GraySourceEventPublisher;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.configuration.GrayServerMarkerConfiguration;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.module.audit.OperateAuditModule;
import cn.springcloud.gray.server.module.audit.jpa.JPAOperateAuditModule;
import cn.springcloud.gray.server.module.gray.GrayInstanceRecordEvictor;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.GrayServerTrackModule;
import cn.springcloud.gray.server.module.gray.GrayServiceIdFinder;
import cn.springcloud.gray.server.module.gray.jpa.JPAGrayInstanceRecordEvictor;
import cn.springcloud.gray.server.module.gray.jpa.JPAGrayServerModule;
import cn.springcloud.gray.server.module.gray.jpa.JPAGrayServerTrackModule;
import cn.springcloud.gray.server.module.gray.jpa.JPAGrayServiceIdFinder;
import cn.springcloud.gray.server.module.user.JPAServiceManageModule;
import cn.springcloud.gray.server.module.user.JPAUserModule;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.oauth2.Oauth2Service;
import cn.springcloud.gray.server.service.GrayDecisionService;
import cn.springcloud.gray.server.service.GrayInstanceService;
import cn.springcloud.gray.server.service.GrayPolicyService;
import cn.springcloud.gray.server.service.GrayServiceService;
import cn.springcloud.gray.server.service.GrayTrackService;
import cn.springcloud.gray.server.service.OperateRecordService;
import cn.springcloud.gray.server.service.ServiceOwnerService;
import cn.springcloud.gray.server.service.UserService;
import cn.springcloud.gray.server.service.UserServiceAuthorityService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableJpaRepositories(basePackages={"cn.springcloud.gray.server.dao.repository"})
@EntityScan(basePackages={"cn.springcloud.gray.server.dao.model"})
@EnableTransactionManagement
@ConditionalOnBean(value={GrayServerMarkerConfiguration.GrayServerMarker.class})
public class DBStorageConfiguration {

    @Configuration
    @ConditionalOnProperty(value={"gray.server.instance.eviction.enabled"})
    public static class JPAGrayInstanceRecordEvictionConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @Transactional
        public GrayInstanceRecordEvictor grayInstanceRecordEvictor(GrayInstanceService grayInstanceService, GrayServerProperties grayServerProperties) {
            GrayServerProperties.InstanceRecordEvictProperties evictProperties = grayServerProperties.getInstance().getEviction();
            return new JPAGrayInstanceRecordEvictor(grayInstanceService, evictProperties.getEvictionInstanceStatus(), evictProperties.getLastUpdateDateExpireDays());
        }
    }

    @ComponentScan(basePackages={"cn.springcloud.gray.server.dao.mapper", "cn.springcloud.gray.server.service"})
    @Configuration
    public class DBGrayServrConfiguration {
        @Autowired
        private GrayServerProperties grayServerProperties;

        @Bean
        @ConditionalOnMissingBean
        public GrayServerModule grayServerModule(GraySourceEventPublisher graySourceEventPublisher, @Autowired(required=false) ServiceDiscovery serviceDiscovery, GrayServiceService grayServiceService, GrayInstanceService grayInstanceService, GrayDecisionService grayDecisionService, GrayPolicyService grayPolicyService, ServiceManageModule serviceManageModule) {
            return new JPAGrayServerModule(this.grayServerProperties, graySourceEventPublisher, serviceDiscovery, grayServiceService, grayInstanceService, grayDecisionService, grayPolicyService, serviceManageModule);
        }

        @Bean
        @ConditionalOnMissingBean
        public GrayServerTrackModule grayServerTrackModule(GraySourceEventPublisher graySourceEventPublisher, GrayTrackService grayTrackService) {
            return new JPAGrayServerTrackModule(graySourceEventPublisher, grayTrackService);
        }

        @Bean
        @ConditionalOnMissingBean
        public GrayServiceIdFinder grayServiceIdFinder(GrayInstanceService grayInstanceService, GrayPolicyService grayPolicyService, GrayDecisionService grayDecisionService, GrayTrackService grayTrackService) {
            return new JPAGrayServiceIdFinder(grayInstanceService, grayPolicyService, grayDecisionService, grayTrackService);
        }

        @Bean
        @ConditionalOnMissingBean
        public UserModule userModule(UserService userService, Oauth2Service oauth2Service) {
            return new JPAUserModule(userService, oauth2Service);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceManageModule serviceManageModule(UserModule userModule, UserServiceAuthorityService userServiceAuthorityService, ServiceOwnerService serviceOwnerService) {
            return new JPAServiceManageModule(userModule, userServiceAuthorityService, serviceOwnerService);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value={"gray.server.operate.audit.enable"}, matchIfMissing=true)
        public OperateAuditModule operateAuditModule(OperateRecordService operateRecordService) {
            return new JPAOperateAuditModule(operateRecordService);
        }
    }

}

