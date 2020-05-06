/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.web.tracker;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.web.HttpRequest;
import cn.springcloud.gray.web.tracker.HttpGrayInfoTracker;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpReceiveGrayInfoTracker
implements HttpGrayInfoTracker {
    private static final Logger log = LoggerFactory.getLogger(HttpReceiveGrayInfoTracker.class);
    private Map<String, Consumer<LoadSpec>> loaders = new HashMap<String, Consumer<LoadSpec>>();

    public HttpReceiveGrayInfoTracker() {
        this.init();
    }

    public void call(GrayHttpTrackInfo trackInfo, HttpRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.startsWith("_g_t_")) continue;
            String[] names = headerName.split("__");
            this.loadGrayTrackInfo(new LoadSpec(headerName, names, trackInfo, request));
        }
    }

    @Override
    public void call(TrackArgs<GrayHttpTrackInfo, HttpRequest> args) {
        this.call(args.getTrackInfo(), args.getRequest());
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void loadGrayTrackInfo(LoadSpec loadSpec) {
        String[] names = loadSpec.getNames();
        Optional.ofNullable(this.loaders.get(names[0])).ifPresent(loader -> loader.accept(loadSpec));
    }

    private void init() {
        this.loaders.put("_g_t_attr", loadSpec -> {
            String value = loadSpec.getHeaderValue();
            loadSpec.getTrackInfo().setAttribute(loadSpec.getNames()[1], loadSpec.getHeaderValue());
            log.debug("\u63a5\u6536\u5230{} --> {}", (Object)loadSpec.getHeaderName(), (Object)value);
        });
        this.loaders.put("_g_t_header", loadSpec -> {
            List<String> value = loadSpec.getHeaderValues();
            loadSpec.getTrackInfo().setHeader(loadSpec.getNames()[1], value);
            log.debug("\u63a5\u6536\u5230{} --> {}", (Object)loadSpec.getHeaderName(), (Object)value);
        });
        this.loaders.put("_g_t_param", loadSpec -> {
            List<String> value = loadSpec.getHeaderValues();
            loadSpec.getTrackInfo().setParameters(loadSpec.getNames()[1], value);
            log.debug("\u63a5\u6536\u5230{} --> {}", (Object)loadSpec.getHeaderName(), (Object)value);
        });
    }

    private static class LoadSpec {
        private String headerName;
        private String[] names;
        private GrayHttpTrackInfo trackInfo;
        private HttpRequest request;

        public String getHeaderValue() {
            return this.request.getHeader(this.headerName);
        }

        public List<String> getHeaderValues() {
            Enumeration<String> ve = this.request.getHeaders(this.headerName);
            ArrayList<String> values = new ArrayList<String>();
            while (ve.hasMoreElements()) {
                values.add(ve.nextElement());
            }
            return values;
        }

        public String getHeaderName() {
            return this.headerName;
        }

        public String[] getNames() {
            return this.names;
        }

        public GrayHttpTrackInfo getTrackInfo() {
            return this.trackInfo;
        }

        public HttpRequest getRequest() {
            return this.request;
        }

        public LoadSpec(String headerName, String[] names, GrayHttpTrackInfo trackInfo, HttpRequest request) {
            this.headerName = headerName;
            this.names = names;
            this.trackInfo = trackInfo;
            this.request = request;
        }
    }

}

