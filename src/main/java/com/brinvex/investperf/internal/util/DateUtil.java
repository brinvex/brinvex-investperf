package com.brinvex.investperf.internal.util;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate maxDate(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    public static LocalDate minDate(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) ? date1 : date2;
    }
}
