/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.ListUtils
 */
package cn.springcloud.gray.decision.compare;

import cn.springcloud.gray.decision.compare.PredicateComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;

public class CollectionStringEqualComparator
implements PredicateComparator<Collection<String>> {
    @Override
    public boolean test(Collection<String> src, Collection<String> another) {
        if (CollectionUtils.isEmpty(src)) {
            return CollectionUtils.isEmpty(another);
        }
        if (CollectionUtils.isEmpty(another)) {
            return false;
        }
        ArrayList<String> srcSorted = new ArrayList<String>(src);
        Collections.sort(srcSorted);
        ArrayList<String> anotherSorted = new ArrayList<String>(another);
        Collections.sort(anotherSorted);
        return ListUtils.isEqualList(srcSorted, anotherSorted);
    }
}

