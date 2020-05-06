/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.factory.AbstractAssignFieldGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class HttpAssignFieldGrayDecisionFactory<C extends AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig>
extends AbstractAssignFieldGrayDecisionFactory<C> {
    public static final String FIELD_SCOPE_HTTP_HEADER = "HttpHeader";
    public static final String FIELD_SCOPE_HTTP_PARAMETER = "HttpParameter";
    public static final String FIELD_SCOPE_HTTP_TRACK_HEADER = "HttpTrackHeader";
    public static final String FIELD_SCOPE_HTTP_TRACK_PARAMETER = "HttpTrackParameter";
    private static Map<String, BiFunction<GrayHttpRequest, String, ?>> fieldValueGetters = new HashMap();

    protected HttpAssignFieldGrayDecisionFactory(Class<C> configClass) {
        super(configClass);
    }

    protected Object getRequestAssignFieldValue(GrayHttpRequest grayRequest, C configBean) {
        Object fieldValue = super.getRequestAssignFieldValue(grayRequest, configBean);
        if (fieldValue != null) {
            return fieldValue;
        }
        switch (((AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig)configBean).getType()) {
            case "HttpHeader": {
                return grayRequest.getHeader(((AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig)configBean).getField());
            }
            case "HttpParameter": {
                return grayRequest.getParameter(((AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig)configBean).getField());
            }
            case "HttpTrackHeader": {
                if (grayRequest.getGrayTrackInfo() == null) {
                    return null;
                }
                return ((GrayHttpTrackInfo)grayRequest.getGrayTrackInfo()).getHeader(((AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig)configBean).getField());
            }
            case "HttpTrackParameter": {
                if (grayRequest.getGrayTrackInfo() == null) {
                    return null;
                }
                return ((GrayHttpTrackInfo)grayRequest.getGrayTrackInfo()).getParameter(((AbstractAssignFieldGrayDecisionFactory.AssignFieldConfig)configBean).getField());
            }
        }
        return null;
    }
}

