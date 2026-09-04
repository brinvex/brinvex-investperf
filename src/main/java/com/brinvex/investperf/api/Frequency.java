package com.brinvex.investperf.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

public enum Frequency {

    MONTH,

    QUARTER,

    YEAR;

    public LocalDate adjustToEndDateIncl(LocalDate date) {
        return switch (this) {
            case MONTH -> date.withDayOfMonth(1).plusMonths(1).minusDays(1);
            case QUARTER -> date.with(IsoFields.DAY_OF_QUARTER, 1).plusMonths(3).minusDays(1);
            case YEAR -> date.withDayOfYear(1).plusYears(1).minusDays(1);
        };
    }

    public String caption(LocalDate date) {
        return switch (this) {
            case MONTH -> Lazy.MONTH_CAPTION_FMT.format(date);
            case QUARTER -> Lazy.QUARTER_CAPTION_FMT.format(date);
            case YEAR -> String.valueOf(date.getYear());
        };
    }

    public int countPerYear() {
        return switch (this) {
            case MONTH -> 12;
            case QUARTER -> 4;
            case YEAR -> 1;
        };
    }

    private static class Lazy {
        private static final DateTimeFormatter MONTH_CAPTION_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
        private static final DateTimeFormatter QUARTER_CAPTION_FMT = DateTimeFormatter.ofPattern("yyyy-Q");
    }
}
