/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayTrackInfo;

public class TrackArgs<TRACK extends GrayTrackInfo, REQ> {
    private TRACK trackInfo;
    private REQ request;
    private GrayTrackDefinition trackDefinition;

    public static <TRACK extends GrayTrackInfo, REQ> TrackArgsBuilder<TRACK, REQ> builder() {
        return new TrackArgsBuilder();
    }

    public TRACK getTrackInfo() {
        return this.trackInfo;
    }

    public REQ getRequest() {
        return this.request;
    }

    public GrayTrackDefinition getTrackDefinition() {
        return this.trackDefinition;
    }

    public TrackArgs(TRACK trackInfo, REQ request, GrayTrackDefinition trackDefinition) {
        this.trackInfo = trackInfo;
        this.request = request;
        this.trackDefinition = trackDefinition;
    }

    public static class TrackArgsBuilder<TRACK extends GrayTrackInfo, REQ> {
        private TRACK trackInfo;
        private REQ request;
        private GrayTrackDefinition trackDefinition;

        TrackArgsBuilder() {
        }

        public TrackArgsBuilder<TRACK, REQ> trackInfo(TRACK trackInfo) {
            this.trackInfo = trackInfo;
            return this;
        }

        public TrackArgsBuilder<TRACK, REQ> request(REQ request) {
            this.request = request;
            return this;
        }

        public TrackArgsBuilder<TRACK, REQ> trackDefinition(GrayTrackDefinition trackDefinition) {
            this.trackDefinition = trackDefinition;
            return this;
        }

        public TrackArgs<TRACK, REQ> build() {
            return new TrackArgs<TRACK, REQ>(this.trackInfo, this.request, this.trackDefinition);
        }

        public String toString() {
            return "TrackArgs.TrackArgsBuilder(trackInfo=" + this.trackInfo + ", request=" + this.request + ", trackDefinition=" + (Object)this.trackDefinition + ")";
        }
    }

}

