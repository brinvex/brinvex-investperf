package test.com.brinvex.investperf;

import com.brinvex.investperf.api.DateAmount;
import com.brinvex.investperf.api.LargeFlowRule;
import com.brinvex.investperf.api.PerfAnalysis;
import com.brinvex.investperf.api.PerfAnalysisRequest;
import com.brinvex.investperf.api.PerformanceAnalyzer;
import com.brinvex.investperf.api.PerformanceCalculator.LinkedModifiedDietzTwrCalculator;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.brinvex.investperf.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.investperf.api.Frequency.MONTH;
import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    /*
    A savings account: 2000 all month, all but 1 withdrawn on the 29th, 3.00 of interest credited
    on the 30th. By default the withdrawal is a large flow, so the month is revalued there and its
    whole interest is measured against the 1 left. With no large flows, the month is one Modified
    Dietz period and the interest is measured against the balance averaged over the month.
    */
    private static final PerfCalcRequest SAVINGS_MONTH = PerfCalcRequest.builder()
            .startDateIncl(parse("2026-04-01"))
            .endDateIncl(parse("2026-04-30"))
            .startAssetValueExcl(new BigDecimal("2000"))
            .endAssetValueIncl(new BigDecimal("4.00"))
            .flows(List.of(
                    new DateAmount("2026-04-29", "-1999")))
            .assetValues(List.of(
                    new DateAmount("2026-04-28", "2000")))
            .flowTiming(BEGINNING_OF_DAY)
            .annualization(DO_NOT_ANNUALIZE)
            .build();

    @Test
    void linkedModifiedDietzTwr_largeFlowRevaluesByDefault() {
        BigDecimal twrReturn = PerformanceCalculator.linkedModifiedDietzTwrCalculator().calculateReturn(SAVINGS_MONTH);
        assertEquals("3.000000", twrReturn.toPlainString());
    }

    @Test
    void linkedModifiedDietzTwr_neverLargeFlow() {
        BigDecimal twrReturn = PerformanceCalculator.linkedModifiedDietzTwrCalculator().calculateReturn(SAVINGS_MONTH.toBuilder()
                .largeFlowRule(new LargeFlowRule.Never())
                .build());
        assertEquals("0.001607", twrReturn.toPlainString());
    }

    @Test
    void linkedModifiedDietzTwr_analyzerPassesLargeFlowLevel() {
        PerfAnalysisRequest.PerfAnalysisRequestBuilder reqBuilder = PerfAnalysisRequest.builder()
                .resultStartDateIncl(parse("2026-04-01"))
                .resultEndDateIncl(parse("2026-04-30"))
                .assetValues(List.of(
                        new DateAmount("2026-03-31", "2000"),
                        new DateAmount("2026-04-28", "2000"),
                        new DateAmount("2026-04-30", "4.00")))
                .flows(List.of(
                        new DateAmount("2026-04-29", "-1999")))
                .twrFlowTiming(BEGINNING_OF_DAY)
                .twrCalculatorType(LinkedModifiedDietzTwrCalculator.class)
                .resultFrequency(MONTH)
                .resultRateScale(6);

        PerfAnalysis byDefault = PerformanceAnalyzer.INSTANCE.analyzePerformance(reqBuilder.build()).getFirst();
        PerfAnalysis noLargeFlows = PerformanceAnalyzer.INSTANCE.analyzePerformance(reqBuilder.largeFlowRule(new LargeFlowRule.Never()).build()).getFirst();

        assertEquals("3.000000", byDefault.periodTwr().toPlainString());
        assertEquals("0.001607", noLargeFlows.periodTwr().toPlainString());
    }

    @Test
    void largeFlowRule_isAlwaysStated() {
        assertThrows(IllegalArgumentException.class, () -> SAVINGS_MONTH.toBuilder().largeFlowRule(null).build());
        assertThrows(IllegalArgumentException.class, () -> new LargeFlowRule.AbovePercent(0));
    }
}
