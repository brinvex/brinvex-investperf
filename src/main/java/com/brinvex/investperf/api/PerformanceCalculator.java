package com.brinvex.investperf.api;

import com.brinvex.investperf.internal.LinkedModifiedDietzTwrCalculatorImpl;
import com.brinvex.investperf.internal.ModifiedDietzMwrCalculatorImpl;
import com.brinvex.investperf.internal.TrueTwrCalculatorImpl;

import java.math.BigDecimal;

public interface PerformanceCalculator {

    static TrueTwrCalculator truetwrCalculator() {
        return TrueTwrCalculatorImpl.INSTANCE;
    }

    static LinkedModifiedDietzTwrCalculator linkedModifiedDietzTwrCalculator() {
        return LinkedModifiedDietzTwrCalculatorImpl.INSTANCE;
    }

    static ModifiedDietzMwrCalculator modifiedDietzMwrCalculator() {
        return ModifiedDietzMwrCalculatorImpl.INSTANCE;
    }

    static TwrCalculator twrCalculator() {
        return PerformanceCalculator.truetwrCalculator();
    }

    static TwrCalculator twrCalculator(String twrCalculatorName) {
        return switch (twrCalculatorName) {
            case "TrueTwrCalculator" -> truetwrCalculator();
            case "LinkedModifiedDietzTwrCalculator" -> linkedModifiedDietzTwrCalculator();
            case "TwrCalculator" -> twrCalculator();
            default -> throw new IllegalStateException("Unexpected value: " + twrCalculatorName);
        };
    }

    static MwrCalculator mwrCalculator() {
        return PerformanceCalculator.modifiedDietzMwrCalculator();
    }

    static MwrCalculator mwrCalculator(String twrCalculatorName) {
        return switch (twrCalculatorName) {
            case "ModifiedDietzMwrCalculator" -> modifiedDietzMwrCalculator();
            case "MwrCalculator" -> mwrCalculator();
            default -> throw new IllegalStateException("Unexpected value: " + twrCalculatorName);
        };
    }

    interface TwrCalculator extends PerformanceCalculator {
    }

    interface TrueTwrCalculator extends TwrCalculator {
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Modified_Dietz_method#Linked_return_versus_true_time-weighted_return">
     * https://en.wikipedia.org/wiki/Modified_Dietz_method#Linked_return_versus_true_time-weighted_return</a>
     */
    interface LinkedModifiedDietzTwrCalculator extends TwrCalculator {
    }

    interface MwrCalculator extends PerformanceCalculator {
    }

    interface ModifiedDietzMwrCalculator extends MwrCalculator {
    }

    interface SimpleReturnCalculator extends PerformanceCalculator {
    }

    BigDecimal calculateReturn(PerfCalcRequest perfCalcRequest);

}
