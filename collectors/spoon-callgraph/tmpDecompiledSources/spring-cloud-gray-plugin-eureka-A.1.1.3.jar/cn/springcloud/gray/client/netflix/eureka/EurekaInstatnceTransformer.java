/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  com.netflix.appinfo.InstanceInfo
 *  com.netflix.appinfo.InstanceInfo$InstanceStatus
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.model.InstanceStatus;
import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EurekaInstatnceTransformer {
    private static final Logger log = LoggerFactory.getLogger(EurekaInstatnceTransformer.class);

    public static InstanceInfo.InstanceStatus toEurekaInstanceStatus(InstanceStatus status) {
        switch (status) {
            case UP: {
                return InstanceInfo.InstanceStatus.UP;
            }
            case DOWN: {
                return InstanceInfo.InstanceStatus.DOWN;
            }
            case UNKNOWN: {
                return InstanceInfo.InstanceStatus.UNKNOWN;
            }
            case STARTING: {
                return InstanceInfo.InstanceStatus.STARTING;
            }
            case OUT_OF_SERVICE: {
                return InstanceInfo.InstanceStatus.OUT_OF_SERVICE;
            }
        }
        log.error("\u4e0d\u652f\u6301{}\u7c7b\u578b\u7684\u5b9e\u4f8b\u72b6\u6001", (Object)status);
        throw new UnsupportedOperationException("\u4e0d\u652f\u6301\u7684\u5b9e\u4f8b\u72b6\u6001");
    }

    public static InstanceStatus toGrayInstanceStatus(InstanceInfo.InstanceStatus status) {
        if (status == null) {
            return InstanceStatus.UNKNOWN;
        }
        switch (status) {
            case DOWN: {
                return InstanceStatus.DOWN;
            }
            case UP: {
                return InstanceStatus.UP;
            }
            case STARTING: {
                return InstanceStatus.STARTING;
            }
            case OUT_OF_SERVICE: {
                return InstanceStatus.OUT_OF_SERVICE;
            }
        }
        return InstanceStatus.UNKNOWN;
    }

}

