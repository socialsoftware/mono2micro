/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.compare;

import cn.springcloud.gray.decision.compare.CollectionStringContainAllComparator;
import cn.springcloud.gray.decision.compare.CollectionStringContainAnyComparator;
import cn.springcloud.gray.decision.compare.CollectionStringEqualComparator;
import cn.springcloud.gray.decision.compare.CollectionStringNotContainAllComparator;
import cn.springcloud.gray.decision.compare.CollectionStringNotContainAnyComparator;
import cn.springcloud.gray.decision.compare.CollectionStringUnEqualComparator;
import cn.springcloud.gray.decision.compare.CompareMode;
import cn.springcloud.gray.decision.compare.PredicateComparator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class Comparators {
    private static Map<CompareMode, PredicateComparator<Collection<String>>> collectionStringComparators = new HashMap<CompareMode, PredicateComparator<Collection<String>>>();
    private static Map<CompareMode, PredicateComparator<String>> stringStringComparators = new HashMap<CompareMode, PredicateComparator<String>>();

    public static PredicateComparator<Collection<String>> getCollectionStringComparator(CompareMode mode) {
        return collectionStringComparators.get((Object)mode);
    }

    public static PredicateComparator<String> getStringComparator(CompareMode mode) {
        return stringStringComparators.get((Object)mode);
    }

    public static <C extends Comparable> boolean equals(C arg1, C arg2) {
        return Comparators.compare(arg1, arg2) == 0;
    }

    public static <C extends Comparable> boolean unEquals(C arg1, C arg2) {
        return Comparators.compare(arg1, arg2) != 0;
    }

    public static <C extends Comparable> boolean lt(C arg1, C arg2) {
        if (Objects.isNull(arg1) || Objects.isNull(arg2)) {
            return false;
        }
        return Comparators.compare(arg1, arg2) > 0;
    }

    public static <C extends Comparable> boolean lte(C arg1, C arg2) {
        if (Objects.isNull(arg1) || Objects.isNull(arg2)) {
            return false;
        }
        return Comparators.compare(arg1, arg2) > -1;
    }

    public static <C extends Comparable> boolean gt(C arg1, C arg2) {
        if (Objects.isNull(arg1) || Objects.isNull(arg2)) {
            return false;
        }
        return Comparators.compare(arg2, arg1) > 0;
    }

    public static <C extends Comparable> boolean gte(C arg1, C arg2) {
        if (Objects.isNull(arg1) || Objects.isNull(arg2)) {
            return false;
        }
        return Comparators.compare(arg2, arg1) > -1;
    }

    public static <C extends Comparable> int compare(C arg1, C arg2) {
        if (arg1 == null) {
            return arg2 == null ? 0 : -1;
        }
        if (arg2 == null) {
            return 1;
        }
        return arg1.compareTo(arg2);
    }

    static {
        collectionStringComparators.put(CompareMode.EQUAL, new CollectionStringEqualComparator());
        collectionStringComparators.put(CompareMode.UNEQUAL, new CollectionStringUnEqualComparator());
        collectionStringComparators.put(CompareMode.CONTAINS_ANY, new CollectionStringContainAnyComparator());
        collectionStringComparators.put(CompareMode.CONTAINS_ALL, new CollectionStringContainAllComparator());
        collectionStringComparators.put(CompareMode.NOT_CONTAINS_ALL, new CollectionStringNotContainAllComparator());
        collectionStringComparators.put(CompareMode.NOT_CONTAINS_ANY, new CollectionStringNotContainAnyComparator());
        stringStringComparators.put(CompareMode.EQUAL, (arg1, arg2) -> StringUtils.equals(arg1, arg2));
        stringStringComparators.put(CompareMode.UNEQUAL, (arg1, arg2) -> !StringUtils.equals(arg1, arg2));
        stringStringComparators.put(CompareMode.LT, (arg_0, arg_1) -> Comparators.lt(arg_0, arg_1));
        stringStringComparators.put(CompareMode.LTE, (arg_0, arg_1) -> Comparators.lte(arg_0, arg_1));
        stringStringComparators.put(CompareMode.GT, (arg_0, arg_1) -> Comparators.gt(arg_0, arg_1));
        stringStringComparators.put(CompareMode.GTE, (arg_0, arg_1) -> Comparators.gte(arg_0, arg_1));
    }
}

