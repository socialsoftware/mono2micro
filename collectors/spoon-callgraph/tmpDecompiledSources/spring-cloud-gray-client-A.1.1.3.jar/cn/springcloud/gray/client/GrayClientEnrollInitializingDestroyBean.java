/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayStatus
 *  org.springframework.beans.factory.InitializingBean
 */
package cn.springcloud.gray.client;

import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.GrayClientConfig;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class GrayClientEnrollInitializingDestroyBean
implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(GrayClientEnrollInitializingDestroyBean.class);
    private CommunicableGrayManager grayManager;
    private InstanceLocalInfo instanceLocalInfo;
    private GrayClientConfig clientConfig;

    public GrayClientEnrollInitializingDestroyBean(CommunicableGrayManager grayManager, GrayClientConfig clientConfig, InstanceLocalInfo instanceLocalInfo) {
        this.grayManager = grayManager;
        this.clientConfig = clientConfig;
        this.instanceLocalInfo = instanceLocalInfo;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.clientConfig.isGrayEnroll()) {
            if (this.clientConfig.grayEnrollDealyTimeInMs() > 0) {
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(this.clientConfig.grayEnrollDealyTimeInMs());
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    log.info("\u7070\u5ea6\u6ce8\u518c\u81ea\u8eab\u5b9e\u4f8b...");
                    this.grayRegister();
                }, "GrayEnroll");
                t.start();
            } else {
                this.grayRegister();
            }
        }
    }

    public void shutdown() {
        if (this.instanceLocalInfo.isGray()) {
            this.grayManager.getGrayInformationClient().serviceDownline(this.instanceLocalInfo.getInstanceId());
        }
    }

    private void grayRegister() {
        GrayInstance grayInstance = new GrayInstance();
        grayInstance.setHost(this.instanceLocalInfo.getHost());
        grayInstance.setGrayStatus(GrayStatus.OPEN);
        grayInstance.setInstanceId(this.instanceLocalInfo.getInstanceId());
        grayInstance.setServiceId(this.instanceLocalInfo.getServiceId());
        grayInstance.setPort(Integer.valueOf(this.instanceLocalInfo.getPort()));
        this.grayManager.getGrayInformationClient().addGrayInstance(grayInstance);
        this.instanceLocalInfo.setGray(true);
    }
}

