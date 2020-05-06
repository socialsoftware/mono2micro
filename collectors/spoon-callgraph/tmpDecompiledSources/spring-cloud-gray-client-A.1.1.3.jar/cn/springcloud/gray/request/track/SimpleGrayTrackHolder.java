/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.core.OrderComparator
 */
package cn.springcloud.gray.request.track;

import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayInfoTracker;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.springframework.core.OrderComparator;

public class SimpleGrayTrackHolder
implements GrayTrackHolder {
    private List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers = new ArrayList();
    private Map<String, GrayTrackDefinition> trackDefinitions = new ConcurrentHashMap<String, GrayTrackDefinition>();
    protected Lock lock = new ReentrantLock();

    public SimpleGrayTrackHolder(List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers, List<GrayTrackDefinition> trackDefinitions) {
        this.initGrayInfoTrackers(trackers);
        this.initGrayTrackDefinitions(trackDefinitions);
    }

    private void initGrayTrackDefinitions(Collection<GrayTrackDefinition> trackDefinitions) {
        if (trackDefinitions != null) {
            trackDefinitions.forEach(definition -> {
                if (!this.trackDefinitions.containsKey(definition.getName())) {
                    this.trackDefinitions.put(definition.getName(), (GrayTrackDefinition)definition);
                }
            });
        }
    }

    private void initGrayInfoTrackers(List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers) {
        if (trackers == null) {
            return;
        }
        OrderComparator.sort(trackers);
        this.trackers = trackers;
    }

    @Override
    public List<GrayInfoTracker> getGrayInfoTrackers() {
        return Collections.unmodifiableList(this.trackers);
    }

    @Override
    public Collection<GrayTrackDefinition> getTrackDefinitions() {
        return this.trackDefinitions.values();
    }

    @Override
    public GrayTrackDefinition getGrayTrackDefinition(String name) {
        return this.trackDefinitions.get(name);
    }

    @Override
    public void updateTrackDefinition(GrayTrackDefinition definition) {
        this.lock.lock();
        try {
            this.updateTrackDefinition(this.trackDefinitions, definition);
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public void deleteTrackDefinition(GrayTrackDefinition definition) {
        this.deleteTrackDefinition(definition.getName());
    }

    protected void updateTrackDefinition(Map<String, GrayTrackDefinition> trackDefinitions, GrayTrackDefinition definition) {
        trackDefinitions.put(definition.getName(), definition);
    }

    @Override
    public void deleteTrackDefinition(String name) {
        this.lock.lock();
        try {
            this.trackDefinitions.remove(name);
        }
        finally {
            this.lock.unlock();
        }
    }

    protected void setTrackDefinitions(Map<String, GrayTrackDefinition> trackDefinitions) {
        this.lock.lock();
        try {
            this.trackDefinitions = trackDefinitions;
        }
        finally {
            this.lock.unlock();
        }
    }
}

