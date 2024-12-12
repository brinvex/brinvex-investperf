package com.brinvex.investperf.internal;

import com.brinvex.fintypes.vo.DateAmount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import static java.util.Collections.emptySortedMap;
import static java.util.stream.Collectors.toMap;

public class RequestSanitizer {

    public static Function<LocalDate, BigDecimal> sanitizeAssetValues(
            Function<LocalDate, BigDecimal> assetValuesProvider,
            Map<LocalDate, BigDecimal> assetValuesMap,
            Collection<DateAmount> assetValuesCollection,
            LocalDate startDateIncl,
            LocalDate endDateIncl
    ) {
        Function<LocalDate, BigDecimal> sanitizedAssetValues;
        if (assetValuesProvider != null) {
            sanitizedAssetValues = assetValuesProvider;
        } else if (assetValuesMap != null) {
            sanitizedAssetValues = assetValuesMap::get;
        } else {
            HashMap<LocalDate, BigDecimal> sanitizedAssetValuesMap = new HashMap<>();
            if (assetValuesCollection != null) {
                LocalDate startDateExcl = startDateIncl.minusDays(1);
                for (DateAmount dateAssetValue : assetValuesCollection) {
                    LocalDate date = dateAssetValue.date();
                    BigDecimal assetValue = dateAssetValue.amount();
                    if (!date.isBefore(startDateExcl) && !date.isAfter(endDateIncl)) {
                        BigDecimal oldAssetValue = sanitizedAssetValuesMap.put(date, assetValue);
                        if (oldAssetValue != null && oldAssetValue.compareTo(assetValue) != 0) {
                            throw new IllegalArgumentException((
                                    "The assetValues collection must not contain different entries for the same date; " +
                                    "given: %s, %s, %s")
                                    .formatted(date, oldAssetValue, assetValue));
                        }
                    }
                }
            }
            sanitizedAssetValues = sanitizedAssetValuesMap::get;
        }
        return sanitizedAssetValues;
    }

    public static SortedMap<LocalDate, BigDecimal> sanitizeFlows(
            Map<LocalDate, BigDecimal> flowsMap,
            Collection<DateAmount> flowsCollection,
            LocalDate startDateIncl,
            LocalDate endDateIncl
    ) {
        SortedMap<LocalDate, BigDecimal> sanitizedFlows;
        if (flowsMap != null) {
            if (flowsMap instanceof SortedMap) {
                sanitizedFlows = ((SortedMap<LocalDate, BigDecimal>) flowsMap);
            } else {
                sanitizedFlows = new TreeMap<>(flowsMap);
            }
            if (!sanitizedFlows.isEmpty()) {
                LocalDate firstKey = sanitizedFlows.firstKey();
                LocalDate lastKey = sanitizedFlows.lastKey();
                LocalDate subFirstKey = startDateIncl.isBefore(firstKey) ? firstKey : startDateIncl;
                LocalDate subLastKey = endDateIncl.isAfter(lastKey) ? lastKey : endDateIncl;
                if (subFirstKey.isAfter(subLastKey)) {
                    sanitizedFlows = emptySortedMap();
                } else {
                    sanitizedFlows = sanitizedFlows.subMap(subFirstKey, subLastKey.plusDays(1));
                }
            }
        } else {
            if (flowsCollection != null) {
                sanitizedFlows = flowsCollection
                        .stream()
                        .filter(dateAmount -> !dateAmount.isBefore(startDateIncl) && !dateAmount.isAfter(endDateIncl))
                        .collect(toMap(DateAmount::date, DateAmount::amount, BigDecimal::add, TreeMap::new));
            } else {
                sanitizedFlows = emptySortedMap();
            }
        }
        return sanitizedFlows;
    }
}
