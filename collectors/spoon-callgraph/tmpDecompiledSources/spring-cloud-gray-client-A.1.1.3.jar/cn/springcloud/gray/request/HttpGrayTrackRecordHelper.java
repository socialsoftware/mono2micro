/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.HttpGrayTrackRecordDevice;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.collections.MapUtils;

public class HttpGrayTrackRecordHelper {
    private HttpGrayTrackRecordHelper() {
    }

    public static void record(HttpGrayTrackRecordDevice recordDevice, GrayTrackInfo grayTrackInfo) {
        Map<String, List<String>> trackHeaders;
        Map<String, String> grayAttributes;
        GrayHttpTrackInfo httpTrackInfo = (GrayHttpTrackInfo)grayTrackInfo;
        Map<String, List<String>> trackParameters = httpTrackInfo.getParameters();
        if (MapUtils.isNotEmpty(trackParameters)) {
            HttpGrayTrackRecordHelper.recordGrayTrackInfos(recordDevice, "_g_t_param", trackParameters);
        }
        if (MapUtils.isNotEmpty(trackHeaders = httpTrackInfo.getHeaders())) {
            HttpGrayTrackRecordHelper.recordGrayTrackInfos(recordDevice, "_g_t_header", trackHeaders);
        }
        if (MapUtils.isNotEmpty(grayAttributes = httpTrackInfo.getAttributes())) {
            HttpGrayTrackRecordHelper.recordGrayTrackInfo(recordDevice, "_g_t_attr", grayAttributes);
        }
    }

    private static void recordGrayTrackInfo(HttpGrayTrackRecordDevice recordDevice, String grayPrefix, Map<String, String> infos) {
        String prefix = grayPrefix + "__";
        if (MapUtils.isNotEmpty(infos)) {
            infos.entrySet().forEach(entry -> recordDevice.record(prefix + (String)entry.getKey(), (String)entry.getValue()));
        }
    }

    private static void recordGrayTrackInfos(HttpGrayTrackRecordDevice recordDevice, String grayPrefix, Map<String, List<String>> infos) {
        String prefix = grayPrefix + "__";
        infos.entrySet().forEach(entry -> recordDevice.record(prefix + (String)entry.getKey(), (List)entry.getValue()));
    }
}

