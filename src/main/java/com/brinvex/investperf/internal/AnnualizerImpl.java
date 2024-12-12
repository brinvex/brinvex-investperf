package com.brinvex.investperf.internal;

import com.brinvex.investperf.api.AnnualizationOption;
import com.brinvex.investperf.api.Annualizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class AnnualizerImpl implements Annualizer {


    @Override
    public BigDecimal annualizeGrowthFactor(
            AnnualizationOption annualizationOption,
            BigDecimal cumulGrowthFactor,
            LocalDate startDateIncl,
            LocalDate endDateIncl
    ) {
        if (annualizationOption == AnnualizationOption.DO_NOT_ANNUALIZE) {
            return cumulGrowthFactor;
        }
        if (cumulGrowthFactor.compareTo(ZERO) == 0) {
            return ZERO;
        }
        if (cumulGrowthFactor.compareTo(ONE) == 0) {
            return cumulGrowthFactor;
        }
        LocalDate endDateExcl = endDateIncl.plusDays(1);
        long fullYears = ChronoUnit.YEARS.between(startDateIncl, endDateExcl);
        if (fullYears < 0) {
            throw new IllegalArgumentException("startDateIncl must be before endDateExcl, given: %s, %s".formatted(startDateIncl, endDateExcl));
        }
        long days = ChronoUnit.DAYS.between(startDateIncl.plusYears(fullYears), endDateExcl);
        if (annualizationOption == AnnualizationOption.ANNUALIZE_IF_OVER_ONE_YEAR) {
            if (fullYears == 0 || (fullYears == 1 && days == 0)) {
                return cumulGrowthFactor;
            }
        }
        if (fullYears == 0 && days == 0) {
            throw new IllegalArgumentException("startDateIncl must be before endDateExcl, given: %s, %s".formatted(startDateIncl, endDateExcl));
        }
        double cumGrowthFactor = cumulGrowthFactor.doubleValue();
        double exponent = 1.0 / (fullYears + (days / 365.0));
        return BigDecimal.valueOf(Math.pow(cumGrowthFactor, exponent));
    }

    @Override
    public BigDecimal annualizeGrowthFactor(
            AnnualizationOption annualizationOption,
            BigDecimal cumulGrowthFactor,
            int fullYears
    ) {
        if (annualizationOption == AnnualizationOption.DO_NOT_ANNUALIZE) {
            return cumulGrowthFactor;
        }
        if (cumulGrowthFactor.compareTo(ZERO) == 0) {
            return ZERO;
        }
        if (cumulGrowthFactor.compareTo(ONE) == 0) {
            return cumulGrowthFactor;
        }
        if (annualizationOption == AnnualizationOption.ANNUALIZE_IF_OVER_ONE_YEAR) {
            if (fullYears == 0 || fullYears == 1) {
                return cumulGrowthFactor;
            }
        }
        if (fullYears == 0) {
            throw new IllegalArgumentException("fullYears must be positive, given: %s".formatted(fullYears));
        }
        if (fullYears == 1) {
            return cumulGrowthFactor;
        }
        double cumGrowthFactor = cumulGrowthFactor.doubleValue();
        double exponent = 1.0 / fullYears;
        return BigDecimal.valueOf(Math.pow(cumGrowthFactor, exponent));
    }

}
