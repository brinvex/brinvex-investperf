package test.com.brinvex.investperf;

import com.brinvex.investperf.api.DateAmount;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.brinvex.investperf.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkedModifiedDietzTwrCalculatorTest {

    @Test
    void linkedModifiedDietzTwr_readmeExample() {
        PerformanceCalculator.LinkedModifiedDietzTwrCalculator linkedTwrCalculator = PerformanceCalculator.linkedModifiedDietzTwrCalculator();
        BigDecimal twrReturn = linkedTwrCalculator.calculateReturn(com.brinvex.investperf.api.PerfCalcRequest.builder()
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

    /*
    The account is emptied on the 14th and refilled on the 17th. The deposit is a large flow
    against the value before the withdrawal, so the empty days form a sub-period of their own
    that starts and ends at zero - it neither gains nor loses.
    */
    @Test
    void linkedModifiedDietzTwr_emptySubPeriodBetweenLargeFlows() {
        BigDecimal twrReturn = PerformanceCalculator.linkedModifiedDietzTwrCalculator().calculateReturn(PerfCalcRequest.builder()
                .startDateIncl(parse("2026-08-01"))
                .endDateIncl(parse("2026-08-31"))
                .startAssetValueExcl(new BigDecimal("1000"))
                .endAssetValueIncl(new BigDecimal("505"))
                .flows(List.of(
                        new DateAmount("2026-08-14", "-1000"),
                        new DateAmount("2026-08-17", "500")))
                .assetValues(List.of(
                        new DateAmount("2026-08-13", "1000"),
                        new DateAmount("2026-08-16", "0")))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .build());
        assertEquals("0.010000", twrReturn.toPlainString());
    }

    /*
    The same, but the deposit is too small against the value before the withdrawal to end the
    sub-period, so the sub-period itself runs empty until the deposit arrives.
    */
    @Test
    void linkedModifiedDietzTwr_subPeriodEmptyUntilSmallFlow() {
        BigDecimal twrReturn = PerformanceCalculator.linkedModifiedDietzTwrCalculator().calculateReturn(PerfCalcRequest.builder()
                .startDateIncl(parse("2026-08-01"))
                .endDateIncl(parse("2026-08-31"))
                .startAssetValueExcl(new BigDecimal("10000"))
                .endAssetValueIncl(new BigDecimal("101"))
                .flows(List.of(
                        new DateAmount("2026-08-14", "-10000"),
                        new DateAmount("2026-08-20", "100")))
                .assetValues(List.of(
                        new DateAmount("2026-08-13", "10000"),
                        new DateAmount("2026-08-19", "0")))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .build());
        assertEquals("0.010000", twrReturn.toPlainString());
    }
}
