/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.GraySourceEventPublisher
 *  cn.springcloud.gray.event.SourceType
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.gray.jpa;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.GraySourceEventPublisher;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.server.module.gray.GrayServerTrackModule;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import cn.springcloud.gray.server.service.GrayTrackService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class JPAGrayServerTrackModule
implements GrayServerTrackModule {
    private GraySourceEventPublisher graySourceEventPublisher;
    private GrayTrackService grayTrackService;

    public JPAGrayServerTrackModule(GraySourceEventPublisher graySourceEventPublisher, GrayTrackService grayTrackService) {
        this.graySourceEventPublisher = graySourceEventPublisher;
        this.grayTrackService = grayTrackService;
    }

    @Override
    public Page<GrayTrack> listGrayTracks(String serviceId, Pageable pageable) {
        return this.grayTrackService.listGrayTracks(serviceId, pageable);
    }

    @Override
    public Page<GrayTrack> listGrayTracks(Pageable pageable) {
        return this.grayTrackService.listGrayTracks(pageable);
    }

    @Override
    public List<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String serviceId) {
        return this.grayTrackService.listGrayTracksEmptyInstanceByServiceId(serviceId);
    }

    @Override
    public List<GrayTrack> listGrayTracksByInstanceId(String instanceId) {
        return this.grayTrackService.listGrayTracksByInstanceId(instanceId);
    }

    @Override
    public Page<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String serviceId, Pageable pageable) {
        return this.grayTrackService.listGrayTracksEmptyInstanceByServiceId(serviceId, pageable);
    }

    @Override
    public Page<GrayTrack> listGrayTracksByInstanceId(String instanceId, Pageable pageable) {
        return this.grayTrackService.listGrayTracksByInstanceId(instanceId, pageable);
    }

    @Override
    public void deleteGrayTrack(Long id) {
        GrayTrack grayTrack = this.getGrayTrack(id);
        this.grayTrackService.delete(id);
        this.publishGrayTrackEvent(EventType.DOWN, grayTrack);
    }

    @Override
    public GrayTrack getGrayTrack(Long id) {
        return (GrayTrack)this.grayTrackService.findOneModel(id);
    }

    @Override
    public GrayTrack saveGrayTrack(GrayTrack track) {
        GrayTrack pre = null;
        if (track.getId() != null) {
            pre = (GrayTrack)this.grayTrackService.findOneModel(track.getId());
        }
        GrayTrack newRecord = this.grayTrackService.saveModel(track);
        if (!(pre == null || StringUtils.equals(pre.getServiceId(), track.getServiceId()) && StringUtils.equals(pre.getInstanceId(), track.getInstanceId()))) {
            this.publishGrayTrackEvent(EventType.DOWN, pre);
        }
        this.publishGrayTrackEvent(EventType.UPDATE, track);
        return newRecord;
    }

    protected void publishGrayTrackEvent(EventType eventType, GrayTrack grayTrack) {
        GrayEventMsg eventMsg = new GrayEventMsg();
        eventMsg.setInstanceId(grayTrack.getInstanceId());
        eventMsg.setServiceId(grayTrack.getServiceId());
        eventMsg.setEventType(eventType);
        eventMsg.setSourceType(SourceType.GRAY_TRACK);
        this.graySourceEventPublisher.asyncPublishEvent(eventMsg, (Object)grayTrack);
    }
}

