/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.gray;

import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GrayServerTrackModule {
    public Page<GrayTrack> listGrayTracks(String var1, Pageable var2);

    public Page<GrayTrack> listGrayTracks(Pageable var1);

    public List<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String var1);

    public List<GrayTrack> listGrayTracksByInstanceId(String var1);

    public Page<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String var1, Pageable var2);

    public Page<GrayTrack> listGrayTracksByInstanceId(String var1, Pageable var2);

    public void deleteGrayTrack(Long var1);

    public GrayTrack getGrayTrack(Long var1);

    public GrayTrack saveGrayTrack(GrayTrack var1);
}

