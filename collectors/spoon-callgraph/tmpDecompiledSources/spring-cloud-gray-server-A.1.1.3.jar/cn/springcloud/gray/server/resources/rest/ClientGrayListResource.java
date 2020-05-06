/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.HttpMethod
 *  org.springframework.http.client.ClientHttpResponse
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.client.RequestCallback
 *  org.springframework.web.client.ResponseExtractor
 *  org.springframework.web.client.RestTemplate
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.server.module.client.ClientRemoteModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value={"/gray/client/grayList"})
public class ClientGrayListResource {
    @Autowired
    private ClientRemoteModule clientRemoteModule;
    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value={"/track/allDefinitions"})
    public ApiRes<List<GrayTrackDefinition>> getAllGrayTracks(@RequestParam(value="serviceId") String serviceId, @RequestParam(value="instanceId") String instanceId) {
        return this.clientRemoteModule.callClient(serviceId, instanceId, "/gray/list/track/allDefinitions", url -> (ApiRes)this.restTemplate.execute(url, HttpMethod.GET, null, res -> (ApiRes)this.objectMapper.readValue(res.getBody(), (TypeReference)new TypeReference<ApiRes<List<GrayTrackDefinition>>>(){}), new Object[0]));
    }

    @GetMapping(value={"/service/allInfos"})
    public ApiRes<Map<String, List<GrayInstance>>> getAllGrayServiceInfos(@RequestParam(value="serviceId") String serviceId, @RequestParam(value="instanceId") String instanceId) {
        return this.clientRemoteModule.callClient(serviceId, instanceId, "/gray/list/service/allInfos", url -> (ApiRes)this.restTemplate.execute(url, HttpMethod.GET, null, res -> (ApiRes)this.objectMapper.readValue(res.getBody(), (TypeReference)new TypeReference<ApiRes<Map<String, List<GrayInstance>>>>(){}), new Object[0]));
    }

}

