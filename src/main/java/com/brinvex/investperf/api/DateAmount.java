package com.brinvex.investperf.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record DateAmount(
        LocalDate date,
        BigDecimal amount
) {

    public DateAmount(String date, String amount) {
        this(LocalDate.parse(date), new BigDecimal(amount));
    }

    public DateAmount(Map.Entry<LocalDate, BigDecimal> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }

    public boolean isBefore(LocalDate date) {
        return this.date.isBefore(date);
    }

    public boolean isAfter(LocalDate date) {
        return this.date.isAfter(date);
    }

    @Override
    public String toString() {
        return "DateAmount{%s/%s}".formatted(date, amount == null ? null : amount.toPlainString());
    }
}
