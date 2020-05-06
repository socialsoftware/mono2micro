/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.request.track;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayInfoTracker;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.utils.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface GrayTrackHolder {
    public List<GrayInfoTracker> getGrayInfoTrackers();

    public Collection<GrayTrackDefinition> getTrackDefinitions();

    public GrayTrackDefinition getGrayTrackDefinition(String var1);

    public void updateTrackDefinition(GrayTrackDefinition var1);

    public void deleteTrackDefinition(GrayTrackDefinition var1);

    public void deleteTrackDefinition(String var1);

    default public <REQ> void recordGrayTrack(GrayTrackInfo info, REQ req) {
        if (!GrayClientHolder.getGraySwitcher().state()) {
            return;
        }
        this.getGrayInfoTrackers().forEach(tracker -> {
            GrayTrackDefinition definition = this.getGrayTrackDefinition(tracker.name());
            if (definition != null) {
                TrackArgs<GrayTrackInfo, Object> args = TrackArgs.builder().trackInfo(info).request(req).trackDefinition(definition).build();
                try {
                    tracker.call(args);
                }
                catch (Exception e) {
                    LogUtils.logger(GrayTrackHolder.class).error(e.getMessage());
                }
            }
        });
    }
}

