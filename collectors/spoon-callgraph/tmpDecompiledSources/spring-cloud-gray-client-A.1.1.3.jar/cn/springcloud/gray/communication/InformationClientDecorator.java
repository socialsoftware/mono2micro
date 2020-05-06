/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.communication;

import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import java.util.List;

public abstract class InformationClientDecorator
implements InformationClient {
    protected abstract <R> R execute(RequestExecutor<R> var1);

    @Override
    public GrayInstance getGrayInstance(final String serviceId, final String instanceId) {
        return this.execute(new RequestExecutor<GrayInstance>(){

            @Override
            public GrayInstance execute(InformationClient delegate) {
                return delegate.getGrayInstance(serviceId, instanceId);
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.GET_GRAY_INSTANCE;
            }
        });
    }

    @Override
    public List<GrayInstance> allGrayInstances() {
        return this.execute(new RequestExecutor<List<GrayInstance>>(){

            @Override
            public List<GrayInstance> execute(InformationClient delegate) {
                return delegate.allGrayInstances();
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.ALL_GRAY_INSTANCES;
            }
        });
    }

    @Override
    public void addGrayInstance(final GrayInstance grayInstance) {
        this.execute(new RequestExecutor<Object>(){

            @Override
            public Object execute(InformationClient delegate) {
                delegate.addGrayInstance(grayInstance);
                return null;
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.ADD_GRAY_INSTANCE;
            }
        });
    }

    @Override
    public void serviceDownline(final String instanceId) {
        this.execute(new RequestExecutor<Object>(){

            @Override
            public Object execute(InformationClient delegate) {
                delegate.serviceDownline(instanceId);
                return null;
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.SERVICE_DOWNLINE;
            }
        });
    }

    @Override
    public List<GrayTrackDefinition> getTrackDefinitions(final String serviceId, final String instanceId) {
        return this.execute(new RequestExecutor<List<GrayTrackDefinition>>(){

            @Override
            public List<GrayTrackDefinition> execute(InformationClient delegate) {
                return delegate.getTrackDefinitions(serviceId, instanceId);
            }

            @Override
            public RequestType getRequestType() {
                return RequestType.GET_TRACK_DEFINITIONS;
            }
        });
    }

    public static interface RequestExecutor<R> {
        public R execute(InformationClient var1);

        public RequestType getRequestType();
    }

    public static enum RequestType {
        ADD_GRAY_INSTANCE,
        SERVICE_DOWNLINE,
        ALL_GRAY_INSTANCES,
        GET_GRAY_INSTANCE,
        GET_TRACK_DEFINITIONS;
        
    }

}

