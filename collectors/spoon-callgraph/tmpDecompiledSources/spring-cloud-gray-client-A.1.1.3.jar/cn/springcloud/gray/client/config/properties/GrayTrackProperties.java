/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import cn.springcloud.gray.model.GrayTrackDefinition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="gray.request.track")
public class GrayTrackProperties {
    private boolean enabled = true;
    private String trackType = "web";
    private Web web = new Web();
    private int definitionsUpdateIntervalTimerInMs = 60000;
    private int definitionsInitializeDelayTimeInMs = 40000;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    public void setWeb(Web web) {
        this.web = web;
    }

    public void setDefinitionsUpdateIntervalTimerInMs(int definitionsUpdateIntervalTimerInMs) {
        this.definitionsUpdateIntervalTimerInMs = definitionsUpdateIntervalTimerInMs;
    }

    public void setDefinitionsInitializeDelayTimeInMs(int definitionsInitializeDelayTimeInMs) {
        this.definitionsInitializeDelayTimeInMs = definitionsInitializeDelayTimeInMs;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getTrackType() {
        return this.trackType;
    }

    public Web getWeb() {
        return this.web;
    }

    public int getDefinitionsUpdateIntervalTimerInMs() {
        return this.definitionsUpdateIntervalTimerInMs;
    }

    public int getDefinitionsInitializeDelayTimeInMs() {
        return this.definitionsInitializeDelayTimeInMs;
    }

    public static class Web {
        public static final String NEED_URI = "uri";
        public static final String NEED_IP = "ip";
        public static final String NEED_METHOD = "method";
        public static final String NEED_HEADERS = "headers";
        public static final String NEED_PARAMETERS = "parameters";
        private String[] pathPatterns = new String[]{"/*"};
        private String[] excludePathPatterns = new String[0];
        private List<GrayTrackDefinition> trackDefinitions = new ArrayList<GrayTrackDefinition>();

        public void setPathPatterns(String[] pathPatterns) {
            this.pathPatterns = pathPatterns;
        }

        public void setExcludePathPatterns(String[] excludePathPatterns) {
            this.excludePathPatterns = excludePathPatterns;
        }

        public void setTrackDefinitions(List<GrayTrackDefinition> trackDefinitions) {
            this.trackDefinitions = trackDefinitions;
        }

        public String[] getPathPatterns() {
            return this.pathPatterns;
        }

        public String[] getExcludePathPatterns() {
            return this.excludePathPatterns;
        }

        public List<GrayTrackDefinition> getTrackDefinitions() {
            return this.trackDefinitions;
        }
    }

}

