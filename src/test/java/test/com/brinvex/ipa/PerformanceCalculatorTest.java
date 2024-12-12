package test.com.brinvex.ipa;

import com.brinvex.finance.types.vo.DateAmount;
import com.brinvex.ipa.api.PerfCalcRequest;
import com.brinvex.ipa.api.PerfCalcRequest.PerfCalcRequestBuilder;
import com.brinvex.ipa.api.PerformanceCalculator;
import com.brinvex.ipa.api.PerformanceCalculator.LinkedModifiedDietzTwrCalculator;
import com.brinvex.ipa.api.PerformanceCalculator.ModifiedDietzMwrCalculator;
import com.brinvex.ipa.api.PerformanceCalculator.TrueTwrCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.brinvex.ipa.api.AnnualizationOption.ANNUALIZE;
import static com.brinvex.ipa.api.AnnualizationOption.ANNUALIZE_IF_OVER_ONE_YEAR;
import static com.brinvex.ipa.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.ipa.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.ipa.api.FlowTiming.END_OF_DAY;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceCalculatorTest {

    private static final TrueTwrCalculator trueTwrCalculator = PerformanceCalculator.truetwrCalculator();

    private static final ModifiedDietzMwrCalculator modifiedDietzMwrCalculator = PerformanceCalculator.modifiedDietzMwrCalculator();

    private static final LinkedModifiedDietzTwrCalculator linkedModifiedDietzTwrCalculator = PerformanceCalculator.linkedModifiedDietzTwrCalculator();

    @Test
    void perfCalc1() {
        PerfCalcRequestBuilder mwrReq1 = PerfCalcRequest.builder()
                .startDateIncl(parse("2020-06-01"))
                .endDateIncl(parse("2020-06-01"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("150"))
                .flows(List.of(
                        new DateAmount(parse("2020-06-01"), new BigDecimal("25"))
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);
        BigDecimal mwr = modifiedDietzMwrCalculator.calculateReturn(mwrReq1.build());
        assertEquals("0.20", mwr.setScale(2, HALF_UP).toPlainString());

        BigDecimal twr = trueTwrCalculator.calculateReturn(mwrReq1.build());

        assertEquals(0, mwr.compareTo(twr));

        mwr = modifiedDietzMwrCalculator.calculateReturn(mwrReq1
                .flowTiming(END_OF_DAY)
                .build());
        assertEquals("0.25", mwr.setScale(2, HALF_UP).toPlainString());

        twr = trueTwrCalculator.calculateReturn(mwrReq1
                .flowTiming(END_OF_DAY)
                .build());

        assertEquals(0, mwr.compareTo(twr));
    }

    @Test
    void perfCalc2() {
        PerfCalcRequestBuilder mwrReq1 = PerfCalcRequest.builder()
                .startDateIncl(parse("2020-01-01"))
                .endDateIncl(parse("2020-12-31"))
                .startAssetValueExcl(new BigDecimal("0"))
                .endAssetValueIncl(new BigDecimal("0"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertThrows(IllegalArgumentException.class, () -> modifiedDietzMwrCalculator.calculateReturn(mwrReq1.copy().build()));
        assertThrows(IllegalArgumentException.class, () -> trueTwrCalculator.calculateReturn(mwrReq1.copy().build()));
        assertThrows(IllegalArgumentException.class, () -> linkedModifiedDietzTwrCalculator.calculateReturn(mwrReq1.copy().build()));
    }

    @Test
    void perfCalc3() {
        PerfCalcRequestBuilder mwrReq1 = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("0"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("-1.000000", modifiedDietzMwrCalculator.calculateReturn(mwrReq1.copy().build()).toPlainString());
        assertEquals("-1.000000", trueTwrCalculator.calculateReturn(mwrReq1.copy().build()).toPlainString());
    }

    @Test
    void perfCalc4_bankruptcy() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("0"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-12-31"), new BigDecimal("100"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("-1.000000", modifiedDietzMwrCalculator.calculateReturn(req.copy().build()).toPlainString());
        assertEquals("-1.000000", trueTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc5_bankruptcy() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("0"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .flowTiming(END_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("-1.000000", modifiedDietzMwrCalculator.calculateReturn(req.copy().build()).toPlainString());
        assertEquals("-1.000000", trueTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc6_bankruptcy() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("0"))
                .flowTiming(END_OF_DAY)
                .annualization(ANNUALIZE_IF_OVER_ONE_YEAR);

        assertEquals("-1.000000", modifiedDietzMwrCalculator.calculateReturn(req.copy().build()).toPlainString());
        assertEquals("-1.000000", trueTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc7() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2022-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("0"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-12-31"), new BigDecimal("0"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("0.020000", trueTwrCalculator.calculateReturn(req.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("0.020000", trueTwrCalculator.calculateReturn(req.copy()
                .annualization(ANNUALIZE).build()).toPlainString());
        assertEquals("0.020000", modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("0.020000", modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .annualization(ANNUALIZE).build()).toPlainString());
    }

    @Test
    void perfCalc8() {
        PerfCalcRequestBuilder req1 = PerfCalcRequest.builder()
                .startDateIncl(parse("2022-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("0"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-12-31"), new BigDecimal("0"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .resultInPercent(true)
                .resultScale(2);

        assertEquals("2.00", modifiedDietzMwrCalculator.calculateReturn(req1.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("2.00", modifiedDietzMwrCalculator.calculateReturn(req1.copy()
                .annualization(ANNUALIZE).build()).toPlainString());

        PerfCalcRequestBuilder req2 = PerfCalcRequest.builder()
                .startDateIncl(parse("2022-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("0"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-12-31"), new BigDecimal("0"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE)
                .resultInPercent(true)
                .resultScale(2);

        assertEquals("2.00", modifiedDietzMwrCalculator.calculateReturn(req2.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("2.00", modifiedDietzMwrCalculator.calculateReturn(req2.copy()
                .annualization(ANNUALIZE).build()).toPlainString());

    }

    @Test
    void perfCalc9() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2022-12-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flows(List.of(
                        new DateAmount(parse("2022-01-01"), new BigDecimal("-2"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-12-31"), new BigDecimal("102"))))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        assertEquals("0.040400", trueTwrCalculator.calculateReturn(req.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("0.020000", trueTwrCalculator.calculateReturn(req.copy()
                .annualization(ANNUALIZE).build()).toPlainString());
        assertEquals("0.040404", modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .annualization(DO_NOT_ANNUALIZE).build()).toPlainString());
        assertEquals("0.020002", modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .annualization(ANNUALIZE).build()).toPlainString());
    }

    @Test
    void perfCalc10() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-01-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);
        assertEquals("0.020000", linkedModifiedDietzTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc11() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-03-31"))
                .startAssetValueExcl(new BigDecimal("100"))
                .endAssetValueIncl(new BigDecimal("102"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);
        assertEquals("0.020000", linkedModifiedDietzTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc12() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-03-31"))
                .startAssetValueExcl(new BigDecimal("10000"))
                .endAssetValueIncl(new BigDecimal("10200"))
                .flows(List.of(
                        new DateAmount(parse("2021-02-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-01-31"), new BigDecimal("10100")),
                        new DateAmount(parse("2021-02-28"), new BigDecimal("10200"))
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);
        assertEquals("0.010000", linkedModifiedDietzTwrCalculator.calculateReturn(req.copy().build()).toPlainString());
    }

    @Test
    void perfCalc13() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-03-31"))
                .startAssetValueExcl(new BigDecimal("10000"))
                .endAssetValueIncl(new BigDecimal("10098"))
                .flows(List.of(
                        new DateAmount(parse("2021-02-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-01-31"), new BigDecimal("10100")),
                        new DateAmount(parse("2021-02-28"), new BigDecimal("10200"))
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        BigDecimal ret1 = linkedModifiedDietzTwrCalculator.calculateReturn(req.copy().build());
        assertEquals("-0.000100", ret1.toPlainString());
    }

    @Test
    void perfCalc15() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2021-01-01"))
                .endDateIncl(parse("2021-03-31"))
                .startAssetValueExcl(new BigDecimal("10000"))
                .endAssetValueIncl(new BigDecimal("10200"))
                .flows(List.of(
                        new DateAmount(parse("2021-02-01"), new BigDecimal("100"))))
                .assetValues(List.of(
                        new DateAmount(parse("2021-01-31"), new BigDecimal("10100")),
                        new DateAmount(parse("2021-02-28"), new BigDecimal("10201"))
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .calcScale(20)
                .resultScale(20)
                .annualization(DO_NOT_ANNUALIZE);

        BigDecimal ret1 = linkedModifiedDietzTwrCalculator.calculateReturn(req.copy().build());
        assertEquals("0.01000000000000000000", ret1.toPlainString());

        BigDecimal ret2 = linkedModifiedDietzTwrCalculator.calculateReturn(req.copy()
                .flows(List.of(
                        new DateAmount(parse("2021-02-15"), new BigDecimal("100"))))
                .build());
        assertEquals("0.01000048773350241428", ret2.toPlainString());
        assertTrue(ret1.compareTo(ret2) < 0);

        BigDecimal ret3 = linkedModifiedDietzTwrCalculator.calculateReturn(req.copy()
                .flows(List.of(
                        new DateAmount(parse("2021-02-28"), new BigDecimal("100"))))
                .build());
        assertEquals("0.01000094495133500625", ret3.toPlainString());
        assertTrue(ret2.compareTo(ret3) < 0, () -> "ret2=%s, ret3=%s".formatted(ret2, ret3));
    }

    @Test
    void perfCalc16() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2023-01-01"))
                .endDateIncl(parse("2023-12-31"))
                .endAssetValueIncl(new BigDecimal("1000"))
                .flowTiming(BEGINNING_OF_DAY)
                .annualization(DO_NOT_ANNUALIZE);

        BigDecimal ret1 = modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .startAssetValueExcl(new BigDecimal("2"))
                .flows(List.of(
                        new DateAmount(parse("2023-12-15"), new BigDecimal("800"))))
                .build());
        BigDecimal ret2 = modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .startAssetValueExcl(new BigDecimal("2"))
                .flows(List.of(
                        new DateAmount(parse("2023-12-16"), new BigDecimal("800"))))
                .build());

        BigDecimal ret3 = modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .startAssetValueExcl(new BigDecimal("1"))
                .flows(List.of(
                        new DateAmount(parse("2023-12-15"), new BigDecimal("800"))))
                .build());
        BigDecimal ret4 = modifiedDietzMwrCalculator.calculateReturn(req.copy()
                .startAssetValueExcl(new BigDecimal("1"))
                .flows(List.of(
                        new DateAmount(parse("2023-12-16"), new BigDecimal("800"))))
                .build());

        assertTrue(ret1.compareTo(ret2) < 0);
        assertTrue(ret3.compareTo(ret4) < 0);
        assertTrue(ret1.compareTo(ret3) < 0, () -> "ret1=%s, ret3=%s".formatted(ret1, ret3));
        assertTrue(ret2.compareTo(ret4) < 0, () -> "ret2=%s, ret4=%s".formatted(ret2, ret4));
    }

    /*
     * https://www.interactivebrokers.com/images/common/Statements/MWR-TWR_white_paper.pdf
     */
    @Test
    void mDietz_ibkr_mwr() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2011-10-01"))
                .endDateIncl(parse("2011-10-31"))
                .startAssetValueExcl(new BigDecimal("4549863.44"))
                .endAssetValueIncl(new BigDecimal("4256598.99"))
                .flows(List.of(
                        new DateAmount(parse("2011-10-04"), new BigDecimal("-225000")),
                        new DateAmount(parse("2011-10-07"), new BigDecimal("81500")),
                        new DateAmount(parse("2011-10-12"), new BigDecimal("-75000")),
                        new DateAmount(parse("2011-10-14"), new BigDecimal("125000")),
                        new DateAmount(parse("2011-10-20"), new BigDecimal("7500"))))
                .flowTiming(END_OF_DAY)
                .resultInPercent(true)
                .resultScale(2)
                .annualization(DO_NOT_ANNUALIZE);

        BigDecimal ret1 = modifiedDietzMwrCalculator.calculateReturn(req.copy().build());
        assertEquals("-4.67", ret1.toPlainString());
    }

    /*
     * https://www.interactivebrokers.com/images/common/Statements/MWR-TWR_white_paper.pdf
     */
    @Test
    void mDietz_ibkr_twr() {
        PerfCalcRequestBuilder req = PerfCalcRequest.builder()
                .startDateIncl(parse("2011-10-03"))
                .endDateIncl(parse("2011-10-07"))
                .startAssetValueExcl(new BigDecimal("4549863.44"))
                .endAssetValueIncl(new BigDecimal("4417916.19"))
                .assetValues(List.of(
                        new DateAmount(parse("2011-10-03"), new BigDecimal("4629129.14")),
                        new DateAmount(parse("2011-10-04"), new BigDecimal("4197829.64")),
                        new DateAmount(parse("2011-10-05"), new BigDecimal("4278627.55")),
                        new DateAmount(parse("2011-10-06"), new BigDecimal("4249124.71"))
                ))
                .flows(List.of(
                        new DateAmount(parse("2011-10-04"), new BigDecimal("-225000")),
                        new DateAmount(parse("2011-10-07"), new BigDecimal("81500"))
                ))
                .flowTiming(BEGINNING_OF_DAY)
                .resultInPercent(true)
                .resultScale(2)
                .annualization(DO_NOT_ANNUALIZE);

        BigDecimal ret1 = trueTwrCalculator.calculateReturn(req.copy().build());
        assertEquals("0.14", ret1.toPlainString());
    }

}
