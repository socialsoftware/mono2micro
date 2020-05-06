/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.decision.factory.AbstractGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;

public class FlowRateGrayDecisionFactory
extends AbstractGrayDecisionFactory<Config> {
    public static final String FIELD_SCOPE_HTTP_HEADER = "HttpHeader";
    public static final String FIELD_SCOPE_HTTP_PARAMETER = "HttpParameter";
    public static final String FIELD_SCOPE_TRACK_ATTRIBUTE = "TrackAttribute";
    public static final String FIELD_SCOPE_HTTP_TRACK_HEADER = "HttpTrackHeader";
    public static final String FIELD_SCOPE_HTTP_TRACK_PARAMETER = "HttpTrackParameter";
    private Map<String, BiFunction<GrayHttpRequest, String, String>> fieldValueGetters = new HashMap<String, BiFunction<GrayHttpRequest, String, String>>();

    public FlowRateGrayDecisionFactory() {
        super(Config.class);
        this.initFieldGetters();
    }

    @Override
    public GrayDecision apply(Config configBean) {
        return args -> {
            GrayHttpRequest grayHttpRequest = (GrayHttpRequest)args.getGrayRequest();
            String value = this.getFieldValue(grayHttpRequest, configBean);
            if (StringUtils.isEmpty(value)) {
                return false;
            }
            int hashcode = Math.abs((value + StringUtils.defaultString(configBean.getSalt())).hashCode());
            int mod = hashcode % 100;
            return mod <= configBean.getRate();
        };
    }

    private void initFieldGetters() {
        this.fieldValueGetters.put(FIELD_SCOPE_HTTP_HEADER, (grayReq, field) -> this.getValueForMultiValueMap((Map<String, ? extends Collection<String>>)grayReq.getHeaders(), (String)field));
        this.fieldValueGetters.put(FIELD_SCOPE_HTTP_PARAMETER, (grayReq, field) -> this.getValueForMultiValueMap((Map<String, ? extends Collection<String>>)grayReq.getParameters(), (String)field));
        this.fieldValueGetters.put(FIELD_SCOPE_TRACK_ATTRIBUTE, (grayReq, field) -> grayReq.getGrayTrackInfo().getAttributes().get(field));
        this.fieldValueGetters.put(FIELD_SCOPE_HTTP_TRACK_HEADER, (grayReq, field) -> {
            GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)grayReq.getGrayTrackInfo();
            if (!Objects.isNull(grayHttpTrackInfo)) {
                return this.joinString(grayHttpTrackInfo.getHeader((String)field));
            }
            return null;
        });
        this.fieldValueGetters.put(FIELD_SCOPE_HTTP_TRACK_PARAMETER, (grayReq, field) -> {
            GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)grayReq.getGrayTrackInfo();
            if (!Objects.isNull(grayHttpTrackInfo)) {
                return this.joinString(grayHttpTrackInfo.getParameter((String)field));
            }
            return null;
        });
    }

    private String getFieldValue(GrayHttpRequest grayRequest, Config configBean) {
        BiFunction<GrayHttpRequest, String, String> func = this.fieldValueGetters.get(configBean.getType());
        if (!Objects.isNull(func)) {
            return func.apply(grayRequest, configBean.getField());
        }
        return null;
    }

    private String getValueForMultiValueMap(Map<String, ? extends Collection<String>> map, String field) {
        return this.joinString(map.get(field));
    }

    private String joinString(Collection<String> collection) {
        if (collection != null) {
            return StringUtils.join(collection, ",");
        }
        return null;
    }

    public static class Config {
        private String type;
        private String field;
        private String salt;
        private int rate;

        public void setType(String type) {
            this.type = type;
        }

        public void setField(String field) {
            this.field = field;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public String getType() {
            return this.type;
        }

        public String getField() {
            return this.field;
        }

        public String getSalt() {
            return this.salt;
        }

        public int getRate() {
            return this.rate;
        }
    }

}

