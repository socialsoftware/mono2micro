/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 *  org.springframework.http.HttpEntity
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.client.RestTemplate
 */
package cn.springcloud.gray.eureka.server.communicate;

import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClient;
import cn.springcloud.gray.model.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpCommunicateClient
implements GrayCommunicateClient {
    private static final Logger log = LoggerFactory.getLogger(HttpCommunicateClient.class);
    private final String baseUrl;
    private RestTemplate rest;

    public HttpCommunicateClient(String baseUrl) {
        this(baseUrl, new RestTemplate());
    }

    public HttpCommunicateClient(String baseUrl, RestTemplate rest) {
        this.baseUrl = baseUrl;
        this.rest = rest;
    }

    @Override
    public void noticeInstanceInfo(InstanceInfo instanceInfo) {
        String url = this.baseUrl + "/gray/discover/instanceInfo";
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity httpEntity = new HttpEntity((Object)instanceInfo, (MultiValueMap)httpHeaders);
            this.rest.postForEntity(url, (Object)httpEntity, null, new Object[0]);
        }
        catch (RuntimeException e) {
            log.error("\u5b9e\u4f8b\u4fe1\u606f\u53d1\u9001\u5931\u8d25", e);
            throw e;
        }
    }
}

