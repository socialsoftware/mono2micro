/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 */
package cn.springcloud.gray.eureka.server.communicate;

import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClient;
import cn.springcloud.gray.model.InstanceInfo;

public abstract class GrayCommunicateClientDecorator
implements GrayCommunicateClient {
    protected abstract <R> R execute(RequestExecutor<R> var1);

    @Override
    public void noticeInstanceInfo(final InstanceInfo instanceInfo) {
        this.execute(new RequestExecutor<Object>(){

            @Override
            public Object execute(GrayCommunicateClient delegate) {
                delegate.noticeInstanceInfo(instanceInfo);
                return null;
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.NOTICE_INSTANCE_INFO;
            }
        });
    }

    public static interface RequestExecutor<R> {
        public R execute(GrayCommunicateClient var1);

        public RequestType getRequestType();
    }

    public static enum RequestType {
        NOTICE_INSTANCE_INFO;
        
    }

}

