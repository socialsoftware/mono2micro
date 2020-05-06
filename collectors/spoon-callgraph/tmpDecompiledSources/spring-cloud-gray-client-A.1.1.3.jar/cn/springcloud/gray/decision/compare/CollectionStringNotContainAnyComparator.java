/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package cn.springcloud.gray.decision.compare;

import cn.springcloud.gray.decision.compare.PredicateComparator;
import java.util.Collection;
import org.apache.commons.collections.CollectionUtils;

public class CollectionStringNotContainAnyComparator
implements PredicateComparator<Collection<String>> {
    @Override
    public boolean test(Collection<String> src, Collection<String> another) {
        if (CollectionUtils.isEmpty(src)) {
            return false;
        }
        if (CollectionUtils.isEmpty(another)) {
            return false;
        }
        for (String v : another) {
            if (!src.contains(v)) continue;
            return false;
        }
        return true;
    }
}

