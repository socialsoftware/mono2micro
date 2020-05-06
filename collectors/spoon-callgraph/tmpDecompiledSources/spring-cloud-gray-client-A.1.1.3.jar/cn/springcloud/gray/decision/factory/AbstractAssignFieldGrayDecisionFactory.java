/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.factory.AbstractGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractAssignFieldGrayDecisionFactory<C extends AssignFieldConfig>
extends AbstractGrayDecisionFactory<C> {
    public static final String FIELD_SCOPE_REQUEST_ATTRIBUTE = "attribute";
    public static final String FIELD_SCOPE_TRACK_ATTRIBUTE = "TrackAttribute";
    private static Map<String, BiFunction<GrayHttpRequest, String, ?>> fieldValueGetters = new HashMap();

    protected AbstractAssignFieldGrayDecisionFactory(Class<C> configClass) {
        super(configClass);
    }

    protected Object getRequestAssignFieldValue(GrayRequest grayRequest, C configBean) {
        switch (((AssignFieldConfig)configBean).getType()) {
            case "attribute": {
                return grayRequest.getAttribute(((AssignFieldConfig)configBean).getField());
            }
            case "TrackAttribute": {
                GrayTrackInfo trackInfo = grayRequest.getGrayTrackInfo();
                if (trackInfo == null) {
                    return null;
                }
                return trackInfo.getAttribute(((AssignFieldConfig)configBean).getField());
            }
        }
        return null;
    }

    protected String getRequestAssignFieldStringValue(GrayRequest grayRequest, C configBean) {
        return this.convertToString(this.getRequestAssignFieldValue(grayRequest, configBean));
    }

    protected String convertToString(Object fieldValue) {
        if (Objects.isNull(fieldValue)) {
            return null;
        }
        if (fieldValue instanceof String) {
            return (String)fieldValue;
        }
        if (fieldValue instanceof Collection) {
            return this.joinString((Collection)fieldValue);
        }
        return fieldValue.toString();
    }

    private String joinString(Collection<String> collection) {
        if (collection != null) {
            return StringUtils.join(collection, ",");
        }
        return null;
    }

    public static class AssignFieldConfig {
        private String type;
        private String field;

        public void setType(String type) {
            this.type = type;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getType() {
            return this.type;
        }

        public String getField() {
            return this.field;
        }
    }

}

