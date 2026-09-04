package com.brinvex.investperf.internal.util;

import java.util.SortedMap;

import static java.util.Collections.emptySortedMap;

public class CollectionUtil {

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> rangeSafeSubMap(SortedMap<K, V> map, K fromKeyIncl, K toKeyExcl) {
        if (map.isEmpty()) {
            return map;
        }
        K oldFirstKey = map.firstKey();
        K oldLastKey = map.lastKey();
        if (fromKeyIncl.compareTo(oldFirstKey) <= 0) {
            if (toKeyExcl.compareTo(oldLastKey) > 0) {
                return map;
            } else if (toKeyExcl.compareTo(oldFirstKey) > 0) {
                return map.headMap(toKeyExcl);
            } else {
                return emptySortedMap();
            }
        } else if (fromKeyIncl.compareTo(oldLastKey) <= 0) {
            if (toKeyExcl.compareTo(oldLastKey) > 0) {
                return map.tailMap(fromKeyIncl);
            } else if (toKeyExcl.compareTo(fromKeyIncl) > 0) {
                return map.subMap(fromKeyIncl, toKeyExcl);
            } else {
                return emptySortedMap();
            }
        } else {
            return emptySortedMap();
        }
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> rangeSafeHeadMap(SortedMap<K, V> map, K toKeyExcl) {
        if (map.isEmpty()) {
            return map;
        }
        K oldFirstKey = map.firstKey();
        K oldLastKey = map.lastKey();
        if (toKeyExcl.compareTo(oldLastKey) > 0) {
            return map;
        } else if (toKeyExcl.compareTo(oldFirstKey) > 0) {
            return map.headMap(toKeyExcl);
        } else {
            return emptySortedMap();
        }
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> rangeSafeTailMap(SortedMap<K, V> map, K fromKeyIncl) {
        if (map.isEmpty()) {
            return map;
        }
        K oldFirstKey = map.firstKey();
        K oldLastKey = map.lastKey();
        if (fromKeyIncl.compareTo(oldFirstKey) <= 0) {
            return map;
        } else if (fromKeyIncl.compareTo(oldLastKey) <= 0) {
            return map.tailMap(fromKeyIncl);
        } else {
            return emptySortedMap();
        }
    }
}
