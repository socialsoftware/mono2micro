/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  cn.springcloud.gray.request.track.GrayTrackHolder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.web.resources;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import java.util.Collection;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/gray/list"})
public class GrayListResource {
    @Autowired
    private GrayTrackHolder grayTrackHolder;
    @Autowired
    private GrayManager grayManager;

    @GetMapping(value={"/service/allInfos"})
    public ApiRes<Map<String, Collection<GrayInstance>>> getAllGrayServiceInfos() {
        return ApiRes.builder().code("0").data((Object)this.grayManager.getMapByAllGrayServices()).build();
    }

    @GetMapping(value={"/track/allDefinitions"})
    public ApiRes<Collection<GrayTrackDefinition>> getAllGrayTracks() {
        return ApiRes.builder().code("0").data((Object)this.grayTrackHolder.getTrackDefinitions()).build();
    }
}

