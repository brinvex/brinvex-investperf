package test.com.brinvex.ipa;

import com.brinvex.finance.types.vo.DateAmount;
import com.brinvex.ipa.api.PerformanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.brinvex.ipa.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.ipa.api.FlowTiming.BEGINNING_OF_DAY;
import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkedModifiedDietzTwrCalculatorTest {

    @Test
    void linkedModifiedDietzTwr_readmeExample() {
        PerformanceCalculator.LinkedModifiedDietzTwrCalculator linkedTwrCalculator = PerformanceCalculator.linkedModifiedDietzTwrCalculator();
        BigDecimal twrReturn = linkedTwrCalculator.calculateReturn(com.brinvex.ipa.api.PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-03-31"))
                .startAssetValueExcl(new BigDecimal("10000"))
                .endAssetValueIncl(new BigDecimal("10200"))
                .flows(List.of(
                        new DateAmount("2021-02-15", "100")))
                .assetValues(List.of(
                        new DateAmount("2021-01-31", "10100"),
                        new DateAmount("2021-02-28", "10201")
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .resultScale(10)
                .annualization(DO_NOT_ANNUALIZE)
                .build());
        assertEquals("0.0100004877", twrReturn.toPlainString());
    }
}
