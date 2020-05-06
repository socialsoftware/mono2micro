/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.utils.WebUtils
 *  javax.servlet.http.HttpServletRequest
 *  org.apache.commons.collections.CollectionUtils
 *  org.aspectj.lang.JoinPoint
 *  org.aspectj.lang.Signature
 *  org.aspectj.lang.annotation.AfterReturning
 *  org.aspectj.lang.annotation.Aspect
 *  org.aspectj.lang.annotation.Pointcut
 *  org.aspectj.lang.reflect.MethodSignature
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.context.request.RequestAttributes
 *  org.springframework.web.context.request.RequestContextHolder
 *  org.springframework.web.context.request.ServletRequestAttributes
 */
package cn.springcloud.gray.server.module.audit;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.audit.OperateAuditModule;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.utils.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class ResultfulOpRecordAspect {
    private static final Logger log = LoggerFactory.getLogger(ResultfulOpRecordAspect.class);
    private String[] recordMethods = new String[]{RequestMethod.POST.name(), RequestMethod.PUT.name(), RequestMethod.DELETE.name()};
    private ObjectMapper objectMapper;
    private UserModule userModule;
    private OperateAuditModule operateAuditModule;
    private Set<String> desensitizationUris = new HashSet<String>(Arrays.asList("/gray/user/", "/gray/user/login", "/gray/user/resetPassword", "/gray/user/updatePassword"));
    private String[] desensitizationFields = new String[]{"password", "oldPassword", "newPassword"};

    public ResultfulOpRecordAspect(ObjectMapper objectMapper, UserModule userModule, OperateAuditModule operateAuditModule) {
        this.objectMapper = objectMapper;
        this.userModule = userModule;
        this.operateAuditModule = operateAuditModule;
    }

    @Pointcut(value="@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping)|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)|| @annotation(org.springframework.web.bind.annotation.PutMapping))")
    public void pointcut() {
    }

    @AfterReturning(value="pointcut()", returning="result")
    public void doAfter(JoinPoint joinPoint, Object result) {
        RequestMapping requestMapping = this.getRequestMapping(joinPoint);
        if (!this.isSholdRecord(requestMapping)) {
            return;
        }
        OperateRecord operateRecord = new OperateRecord();
        operateRecord.setOperateTime(new Date());
        Signature signature = joinPoint.getSignature();
        operateRecord.setHandler(signature.getDeclaringType().getSimpleName() + "#" + signature.getName());
        operateRecord.setOperator(this.userModule.getCurrentUserId());
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        operateRecord.setUri(request.getRequestURI());
        operateRecord.setHttpMethod(request.getMethod());
        operateRecord.setIp(WebUtils.getIpAddr((HttpServletRequest)request));
        operateRecord.setQueryString(request.getQueryString());
        try {
            String HeadlerArgs = this.desensitizationArgs(request, joinPoint.getArgs());
            operateRecord.setHeadlerArgs(this.objectMapper.writeValueAsString(HeadlerArgs));
        }
        catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        if (result instanceof ApiRes) {
            ApiRes apiRes = (ApiRes)result;
            operateRecord.setApiResCode(apiRes.getCode());
            if (StringUtils.equals(apiRes.getCode(), "0")) {
                operateRecord.setOperateState(1);
            }
        }
        this.operateAuditModule.recordOperate(operateRecord);
    }

    private String desensitizationArgs(HttpServletRequest request, Object[] args) throws IOException {
        if (this.desensitizationUris.contains(request.getRequestURI())) {
            List list = (List)this.objectMapper.readValue(this.objectMapper.writeValueAsString(args), (TypeReference)new TypeReference<List<Map<String, Object>>>(){});
            if (CollectionUtils.isNotEmpty((Collection)list)) {
                Map map = (Map)list.get(0);
                for (String field : this.desensitizationFields) {
                    Object v = map.get(field);
                    if (Objects.isNull(v)) continue;
                    map.put(field, this.convertDesensitization(v.toString()));
                }
            }
            return this.objectMapper.writeValueAsString(list);
        }
        return this.objectMapper.writeValueAsString(args);
    }

    private String convertDesensitization(String str) {
        StringBuilder sb = new StringBuilder();
        int l = str.length();
        for (int i = 0; i < l; ++i) {
            sb.append('*');
        }
        return sb.toString();
    }

    private boolean isSholdRecord(RequestMapping requestMapping) {
        if (requestMapping == null) {
            return false;
        }
        return this.isSholdRecord(requestMapping.method()) && this.sholdFromRequest();
    }

    private RequestMapping getRequestMapping(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            RequestMapping requestMapping = (RequestMapping)AnnotationUtils.findAnnotation((Method)signature.getMethod(), RequestMapping.class);
            if (requestMapping == null) {
                return (RequestMapping)AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequestMapping.class);
            }
            return requestMapping;
        }
        return null;
    }

    private boolean isSholdRecord(RequestMethod[] methods) {
        for (RequestMethod method : methods) {
            if (!ArrayUtils.contains(this.recordMethods, method.name())) continue;
            return true;
        }
        return false;
    }

    private boolean sholdFromRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (requestAttributes == null) {
            return false;
        }
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        if (request == null) {
            return false;
        }
        return ArrayUtils.contains(this.recordMethods, request.getMethod());
    }

}

