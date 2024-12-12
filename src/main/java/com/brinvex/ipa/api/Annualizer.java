package com.brinvex.ipa.api;

import com.brinvex.ipa.internal.AnnualizerImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Annualizer {

    Annualizer INSTANCE = new AnnualizerImpl();

    default BigDecimal annualizeReturn(
            AnnualizationOption annualizationOption,
            BigDecimal cumulReturn,
            LocalDate startDateIncl,
            LocalDate endDateIncl
    ) {
        BigDecimal cumulFactor = cumulReturn.add(BigDecimal.ONE);
        BigDecimal annFactor = annualizeGrowthFactor(annualizationOption, cumulFactor, startDateIncl, endDateIncl);
        return annFactor.subtract(BigDecimal.ONE);
    }

    BigDecimal annualizeGrowthFactor(
            AnnualizationOption annualizationOption,
            BigDecimal cumulGrowthFactor,
            LocalDate startDateIncl,
            LocalDate endDateIncl
    );

    BigDecimal annualizeGrowthFactor(
            AnnualizationOption annualizationOption,
            BigDecimal cumulGrowthFactor,
            int fullYears
    );
}
