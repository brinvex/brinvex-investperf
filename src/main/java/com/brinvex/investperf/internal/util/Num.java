package com.brinvex.investperf.internal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Num {

    public static final BigDecimal _100 = new BigDecimal("100");
    public static final BigDecimal MINUS_1 = new BigDecimal("-1");

    public static BigDecimal setScale(BigDecimal bd, int newScale, RoundingMode roundingMode) {
        if (bd == null) {
            return null;
        }
        return bd.setScale(newScale, roundingMode);
    }
}
