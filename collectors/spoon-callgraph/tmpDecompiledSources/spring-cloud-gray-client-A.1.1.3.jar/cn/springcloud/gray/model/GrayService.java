/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 */
package cn.springcloud.gray.model;

import cn.springcloud.gray.model.GrayInstance;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GrayService {
    private String serviceId;
    private Map<String, GrayInstance> grayInstances = new ConcurrentHashMap<String, GrayInstance>();
    private Lock lock = new ReentrantLock();

    public Collection<GrayInstance> getGrayInstances() {
        return this.grayInstances.values();
    }

    public boolean contains(String instanceId) {
        return this.grayInstances.containsKey(instanceId);
    }

    public void setGrayInstance(GrayInstance grayInstance) {
        this.lock.lock();
        try {
            this.grayInstances.put(grayInstance.getInstanceId(), grayInstance);
        }
        finally {
            this.lock.unlock();
        }
    }

    public GrayInstance removeGrayInstance(String instanceId) {
        this.lock.lock();
        try {
            GrayInstance grayInstance = this.grayInstances.remove(instanceId);
            return grayInstance;
        }
        finally {
            this.lock.unlock();
        }
    }

    public GrayInstance getGrayInstance(String instanceId) {
        return this.grayInstances.get(instanceId);
    }

    public boolean isOpenGray() {
        return this.getGrayInstances() != null && !this.getGrayInstances().isEmpty() && this.hasGrayInstance();
    }

    public boolean hasGrayInstance() {
        for (GrayInstance grayInstance : this.getGrayInstances()) {
            if (!grayInstance.isGray()) continue;
            return true;
        }
        return false;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }
}

