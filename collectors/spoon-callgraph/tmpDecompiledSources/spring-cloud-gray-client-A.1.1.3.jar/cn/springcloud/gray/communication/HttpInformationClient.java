/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.core.ParameterizedTypeReference
 *  org.springframework.http.HttpEntity
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpMethod
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.client.RestTemplate
 */
package cn.springcloud.gray.communication;

import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpInformationClient
implements InformationClient {
    private static final Logger log = LoggerFactory.getLogger(HttpInformationClient.class);
    private final String baseUrl;
    private RestTemplate rest;

    public HttpInformationClient(String baseUrl) {
        this(baseUrl, new RestTemplate());
    }

    public HttpInformationClient(String baseUrl, RestTemplate rest) {
        this.baseUrl = baseUrl;
        this.rest = rest;
    }

    @Override
    public List<GrayInstance> allGrayInstances() {
        String url = this.baseUrl + "/gray/instances/enable";
        ParameterizedTypeReference<List<GrayInstance>> typeRef = new ParameterizedTypeReference<List<GrayInstance>>(){};
        try {
            ResponseEntity responseEntity = this.rest.exchange(url, HttpMethod.GET, null, (ParameterizedTypeReference)typeRef, new Object[0]);
            return (List)responseEntity.getBody();
        }
        catch (RuntimeException e) {
            log.error("\u83b7\u53d6\u7070\u5ea6\u670d\u52a1\u5217\u8868\u5931\u8d25", e);
            throw e;
        }
    }

    @Override
    public void addGrayInstance(GrayInstance grayInstance) {
        String url = this.baseUrl + "/gray/instance/";
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity httpEntity = new HttpEntity((Object)grayInstance, (MultiValueMap)httpHeaders);
            this.rest.postForEntity(url, (Object)httpEntity, null, new Object[0]);
        }
        catch (RuntimeException e) {
            log.error("\u7070\u5ea6\u670d\u52a1\u5b9e\u4f8b\u4e0b\u7ebf\u5931\u8d25", e);
            throw e;
        }
    }

    @Override
    public GrayInstance getGrayInstance(String serviceId, String instanceId) {
        String url = this.baseUrl + "/gray/instance?serviceId={serviceId}&instanceId={instanceId}";
        try {
            ResponseEntity responseEntity = this.rest.getForEntity(url, GrayInstance.class, new Object[]{serviceId, instanceId});
            return (GrayInstance)responseEntity.getBody();
        }
        catch (RuntimeException e) {
            log.error("\u83b7\u53d6\u7070\u5ea6\u5b9e\u4f8b", e);
            throw e;
        }
    }

    @Override
    public void serviceDownline(String instanceId) {
        String url = this.baseUrl + "/gray/instance/{id}/switchStatus?switch=0";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", instanceId);
        try {
            this.rest.put(url, null, params);
        }
        catch (Exception e) {
            log.error("\u7070\u5ea6\u670d\u52a1\u5b9e\u4f8b\u4e0b\u7ebf\u5931\u8d25, url:{}, params:{}", url, params, e);
            throw e;
        }
    }

    @Override
    public List<GrayTrackDefinition> getTrackDefinitions(String serviceId, String instanceId) {
        String url = this.baseUrl + "/gray/trackDefinitions?serviceId={serviceId}&instanceId={instanceId}";
        ParameterizedTypeReference<List<GrayTrackDefinition>> typeRef = new ParameterizedTypeReference<List<GrayTrackDefinition>>(){};
        try {
            ResponseEntity responseEntity = this.rest.exchange(url, HttpMethod.GET, null, (ParameterizedTypeReference)typeRef, new Object[]{serviceId, instanceId});
            return (List)responseEntity.getBody();
        }
        catch (RuntimeException e) {
            log.error("\u83b7\u53d6\u7070\u5ea6\u8ffd\u8e2a\u4fe1\u606f", e);
            throw e;
        }
    }

}

