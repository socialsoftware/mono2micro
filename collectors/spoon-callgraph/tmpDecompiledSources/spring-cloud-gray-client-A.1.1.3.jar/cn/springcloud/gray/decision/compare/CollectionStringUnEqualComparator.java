/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.ListUtils
 *  org.springframework.util.CollectionUtils
 */
package cn.springcloud.gray.decision.compare;

import cn.springcloud.gray.decision.compare.PredicateComparator;
import java.util.Collection;
import org.apache.commons.collections.ListUtils;
import org.springframework.util.CollectionUtils;

public class CollectionStringUnEqualComparator
implements PredicateComparator<Collection<String>> {
    @Override
    public boolean test(Collection<String> src, Collection<String> another) {
        if (CollectionUtils.isEmpty(src)) {
            return !CollectionUtils.isEmpty(another);
        }
        if (CollectionUtils.isEmpty(another)) {
            return true;
        }
        return !ListUtils.isEqualList(src, another);
    }
}

