package test.com.brinvex.investperf;

import com.brinvex.fintypes.vo.DateAmount;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerfCalcRequest.PerfCalcRequestBuilder;
import com.brinvex.investperf.api.PerformanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.brinvex.investperf.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.investperf.api.FlowTiming.END_OF_DAY;
import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrueTwrCalculatorTest {

    @Test
    void twr_readmeExample() {
        BigDecimal twrReturn = PerformanceCalculator.twrCalculator().calculateReturn(PerfCalcRequest.builder()
                .startDateIncl(parse("2020-06-01"))
                .endDateIncl(parse("2020-06-30"))
                .startAssetValueExcl(new BigDecimal("100000"))
                .endAssetValueIncl(new BigDecimal("135000"))
                .flows(List.of(
                        new DateAmount("2020-06-06", "-2000"),
                        new DateAmount("2020-06-11", "20000")))
                .assetValues(List.of(
                        new DateAmount("2020-06-05", "101000"),
                        new DateAmount("2020-06-10", "132000")))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .resultInPercent(true)
                .resultScale(4)
                .build());
        assertEquals("19.6053", twrReturn.toPlainString());
    }

    @Test
    void twr1() {
        BigDecimal ret1 = PerformanceCalculator.twrCalculator().calculateReturn(PerfCalcRequest.builder()
                .startDateIncl(parse("2023-01-22"))
                .endDateIncl(parse("2023-01-23"))
                .startAssetValueExcl(new BigDecimal("1000.00"))
                .endAssetValueIncl(new BigDecimal("2000.00"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .build());
        assertEquals("1.000000", ret1.toPlainString());
    }

    @Test
    void twr2() {
        PerfCalcRequestBuilder calcReqBuilder = PerfCalcRequest.builder()
                .startDateIncl(parse("2023-01-01"))
                .endDateIncl(parse("2024-12-31"))
                .startAssetValueExcl(new BigDecimal("1000.00"))
                .endAssetValueIncl(new BigDecimal("2000.00"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("1.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy().build()).toPlainString());

        assertEquals("1.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(END_OF_DAY)
                        .build())
                .toPlainString());

        assertEquals("0.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(BEGINNING_OF_DAY)
                        .assetValues(List.of(
                                new DateAmount(parse("2023-12-31"), new BigDecimal("1000"))))
                        .flows(List.of(
                                new DateAmount(parse("2024-01-01"), new BigDecimal("1000"))))
                        .build())
                .toPlainString());

        assertEquals("0.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(END_OF_DAY)
                        .assetValues(List.of(
                                new DateAmount(parse("2024-12-31"), new BigDecimal("2000"))))
                        .flows(List.of(
                                new DateAmount(parse("2024-12-31"), new BigDecimal("1000"))))
                        .build())
                .toPlainString());

        assertEquals("1.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(BEGINNING_OF_DAY)
                        .assetValues(List.of(
                                new DateAmount(parse("2023-12-31"), new BigDecimal("1000"))))
                        .flows(List.of(
                                new DateAmount(parse("2024-01-01"), new BigDecimal("0"))))
                        .build())
                .toPlainString());

        assertEquals("1.000000", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(BEGINNING_OF_DAY)
                        .assetValues(List.of(
                                new DateAmount(parse("2023-12-31"), new BigDecimal("4000"))))
                        .flows(List.of(
                                new DateAmount(parse("2024-01-01"), new BigDecimal("0"))))
                        .build())
                .toPlainString());

        assertEquals("0.999995", PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                        .flowTiming(BEGINNING_OF_DAY)
                        .assetValues(List.of(
                                new DateAmount(parse("2023-12-31"), new BigDecimal("4000"))))
                        .flows(List.of(
                                new DateAmount(parse("2024-01-01"), new BigDecimal("0.01"))))
                        .build())
                .toPlainString());

        BigDecimal ret8 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .assetValues(List.of(
                        new DateAmount(parse("2023-12-31"), new BigDecimal("4000"))))
                .flows(List.of(
                        new DateAmount(parse("2024-01-01"), new BigDecimal("2000"))))
                .build());
        assertEquals("0.333333", ret8.toPlainString());

        BigDecimal ret9 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .assetValues(List.of(
                        new DateAmount(parse("2023-12-31"), new BigDecimal("4000.01"))))
                .flows(List.of(
                        new DateAmount(parse("2024-01-01"), new BigDecimal("2000"))))
                .build());
        assertEquals("0.333334", ret9.toPlainString());
        assertEquals(1, ret9.compareTo(ret8));

        BigDecimal ret10 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .assetValues(List.of(
                        new DateAmount(parse("2023-12-31"), new BigDecimal("3999.99"))))
                .flows(List.of(
                        new DateAmount(parse("2024-01-01"), new BigDecimal("2000"))))
                .build());
        assertEquals("0.333332", ret10.toPlainString());
        assertEquals(-1, ret10.compareTo(ret8));

        BigDecimal ret11 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .startAssetValueExcl(BigDecimal.ZERO)
                .endAssetValueIncl(new BigDecimal("2000"))
                .assetValues(List.of())
                .flows(List.of(
                        new DateAmount(parse("2023-01-01"), new BigDecimal("1000"))
                ))
                .build());
        assertEquals("1.000000", ret11.toPlainString());

        BigDecimal ret12 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .startAssetValueExcl(BigDecimal.ZERO)
                .endAssetValueIncl(new BigDecimal("2000"))
                .assetValues(List.of())
                .flows(List.of(
                        new DateAmount(parse("2023-01-01"), new BigDecimal("1000"))
                ))
                .build());
        assertEquals("1.000000", ret12.toPlainString());

        BigDecimal ret13 = PerformanceCalculator.twrCalculator().calculateReturn(calcReqBuilder.copy()
                .flowTiming(BEGINNING_OF_DAY)
                .flowTiming(END_OF_DAY)
                .startAssetValueExcl(new BigDecimal("1000"))
                .endAssetValueIncl(BigDecimal.ZERO)
                .assetValues(List.of())
                .flows(List.of(
                        new DateAmount(parse("2024-12-31"), new BigDecimal("-2000"))
                ))
                .build());
        assertEquals("1.000000", ret13.toPlainString());

    }

    @Test
    void twr3() {
        BigDecimal ret13 = PerformanceCalculator.twrCalculator().calculateReturn(PerfCalcRequest.builder()
                .startDateIncl(parse("2023-01-01"))
                .endDateIncl(parse("2024-12-31"))
                .startAssetValueExcl(new BigDecimal("1000.00"))
                .endAssetValueIncl(new BigDecimal("1030.00"))
                .annualization(DO_NOT_ANNUALIZE)
                .flowTiming(END_OF_DAY)
                .assetValues(List.of(
                        new DateAmount(parse("2023-01-03"), new BigDecimal("1010")),
                        new DateAmount(parse("2023-01-04"), new BigDecimal("1030"))
                ))
                .flows(List.of(
                        new DateAmount(parse("2023-01-03"), new BigDecimal("10")),
                        new DateAmount(parse("2023-01-04"), new BigDecimal("20"))
                ))
                .build());
        assertEquals("0.000000", ret13.toPlainString());

    }

    /*
     * https://en.wikipedia.org/wiki/Time-weighted_return#Example_1
     */
    @Test
    void twr_Wikipedia1() {
        PerfCalcRequest twrReq1 = PerfCalcRequest.builder()
                .startDateIncl(parse("2023-01-01"))
                .endDateIncl(parse("2024-12-31"))
                .startAssetValueExcl(new BigDecimal("500.00"))
                .endAssetValueIncl(new BigDecimal("1500.00"))
                .assetValues(List.of(
                        new DateAmount(parse("2023-12-31"), new BigDecimal("1000"))))
                .flows(List.of(
                        new DateAmount(parse("2024-01-01"), new BigDecimal("1000"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .resultScale(2)
                .build();
        assertEquals("0.50", PerformanceCalculator.twrCalculator().calculateReturn(twrReq1).toPlainString());
    }


}
