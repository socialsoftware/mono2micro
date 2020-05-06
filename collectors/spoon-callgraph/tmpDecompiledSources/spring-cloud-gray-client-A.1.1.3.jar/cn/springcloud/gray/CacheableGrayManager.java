/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 */
package cn.springcloud.gray;

import cn.springcloud.gray.Cache;
import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.decision.GrayDecision;
import java.util.List;

public interface CacheableGrayManager
extends UpdateableGrayManager {
    public Cache<String, List<GrayDecision>> getGrayDecisionCache();
}

