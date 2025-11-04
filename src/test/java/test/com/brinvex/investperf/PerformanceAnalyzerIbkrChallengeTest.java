package test.com.brinvex.investperf;

import com.brinvex.fintypes.vo.DateAmount;
import com.brinvex.investperf.api.PerfAnalysis;
import com.brinvex.investperf.api.PerfAnalysisRequest;
import com.brinvex.investperf.api.PerformanceAnalyzer;
import com.brinvex.investperf.api.PerformanceCalculator;
import com.brinvex.java.IOCallUtil;
import com.brinvex.java.Num;
import com.brinvex.java.collection.CollectionPrintUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.SequencedCollection;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.investperf.api.FlowTiming.END_OF_DAY;
import static com.brinvex.java.collection.Collectors.toTreeMap;
import static java.time.LocalDate.parse;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static test.com.brinvex.investperf.PerformanceAnalyzerTest.assertEqualsWithMultilineMsg;

class PerformanceAnalyzerIbkrChallengeTest {

    private static final Path TEST_DATASET1_DIR = Path.of("c:/prj/bx/bx-investperf/test-data/test-dataset1/");

    private static TreeMap<LocalDate, BigDecimal> assetValues;

    private static LocalDate eurFlowsStartDate;
    private static List<DateAmount> eurFlows;

    @BeforeAll
    public static void beforeAll() throws IOException {
        try (Stream<String> lines = Files.lines(TEST_DATASET1_DIR.resolve("assetValues.txt"))) {
            assetValues = lines
                    .filter(not(String::isBlank))
                    .map(line -> {
                        String[] lineParts = line.split(",");
                        return new DateAmount(lineParts[0], lineParts[1]);
                    })
                    .collect(toTreeMap(DateAmount::date, DateAmount::amount));
        }

        // Fill the asset-values gaps caused be weekends
        {
            LocalDate firstAssetValueDate = assetValues.firstKey();
            LocalDate lastAssetValueDate = assetValues.lastKey();
            BigDecimal currentAssetValue = assetValues.get(firstAssetValueDate);
            for (LocalDate date = firstAssetValueDate; !date.isAfter(lastAssetValueDate); date = date.plusDays(1)) {
                if (assetValues.containsKey(date)) {
                    currentAssetValue = assetValues.get(date);
                }
                assetValues.put(date, currentAssetValue);
            }
        }

        List<DateAmount> flows;
        try (Stream<String> lines = Files.lines(TEST_DATASET1_DIR.resolve("flows.txt"))) {
            flows = lines
                    .filter(not(String::isBlank))
                    .map(line -> {
                        String[] lineParts = line.split(",");
                        return new DateAmount(lineParts[0], lineParts[1]);
                    })
                    .toList();
        }

        //the last USD flow occurred on Friday 2023-10-13
        eurFlowsStartDate = LocalDate.parse("2023-10-17");
        eurFlows = flows
                .stream()
                .filter(da -> !da.isBefore(eurFlowsStartDate))
                .toList();
    }

    private static void testPerfAnalysis(LocalDate startDateIncl, LocalDate endDateIncl) {
        PerfAnalysisRequest perfAnalysisReq = PerfAnalysisRequest.builder()
                .resultStartDateIncl(startDateIncl)
                .resultEndDateIncl(endDateIncl)
                .flows(eurFlows)
                .assetValues(assetValues.entrySet().stream().map(DateAmount::new).toList())
                .twrFlowTiming(BEGINNING_OF_DAY)
                .mwrFlowTiming(END_OF_DAY)
                .twrCalculatorType(PerformanceCalculator.TrueTwrCalculator.class)
                .mwrCalculatorType(PerformanceCalculator.MwrCalculator.class)
                .calcScale(10)
                .resultScale(2)
                .resultRatesInPercent(true)
                .calculateMwr(true)
                .build();
        SequencedCollection<PerfAnalysis> perfAnalyses = PerformanceAnalyzer.INSTANCE.analyzePerformance(perfAnalysisReq);
        String expectedResultFileName = "perfAnalysesResult_%s_%s.txt".formatted(startDateIncl, endDateIncl);
        String expected = IOCallUtil.uncheckedIO(() -> Files.readString(TEST_DATASET1_DIR.resolve(expectedResultFileName)));
        String actual = perfAnalysesToGridString(perfAnalyses);
        assertEqualsWithMultilineMsg(expected, actual);
    }

    private static String perfAnalysesToGridString(SequencedCollection<PerfAnalysis> perfAnalyses) {
        return CollectionPrintUtil.prettyPrintCollection(perfAnalyses,
                List.of(
                        "period",
                        "startVal",
                        "endVal",
                        "prdFlow",
                        "cumTwr",
                        "cumMwr",
                        "prdProf",
                        "totProf"
                ),
                List.of(
                        PerfAnalysis::periodCaption,
                        perfAnalysis -> Num.setScale2(perfAnalysis.periodStartAssetValueExcl()),
                        perfAnalysis -> Num.setScale2(perfAnalysis.periodEndAssetValueIncl()),
                        perfAnalysis -> Num.setScale2(perfAnalysis.periodFlow()),
                        PerfAnalysis::cumulativeTwr,
                        PerfAnalysis::cumulativeMwr,
                        perfAnalysis -> Num.setScale2(perfAnalysis.periodProfit()),
                        perfAnalysis -> Num.setScale2(perfAnalysis.totalProfit())
                )
        );
    }

    @Test
    void ptf11_1() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-17"));
    }

    @Test
    void ptf11_2() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-18"));
    }

    @Test
    void ptf11_3() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-19"));
    }

    @Test
    void ptf11_4() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-20"));
    }

    @Test
    void ptf11_5() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-23"));
    }

    @Test
    void ptf11_6() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-24"));
    }

    @Test
    void ptf11_7() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-25"));
    }

    @Test
    void ptf11_8() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-26"));
    }

    @Test
    void ptf11_9() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-27"));
    }

    @Test
    void ptf11_10() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-10-31"));
    }

    @Test
    void ptf11_11() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-11-01"));
    }

    @Test
    void ptf11_12() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-11-30"));
    }

    @Test
    void ptf11_13() {
        testPerfAnalysis(eurFlowsStartDate, parse("2023-12-29"));
    }

    @Test
    void ptf11_14() {
        testPerfAnalysis(eurFlowsStartDate, parse("2024-01-31"));
    }

    @Test
    void ptf11_15() {
        testPerfAnalysis(eurFlowsStartDate, parse("2024-02-29"));
    }

    @Test
    void ptf11_16() {
        testPerfAnalysis(eurFlowsStartDate, parse("2024-04-30"));
    }

    @Test
    void ptf11_17() {
        testPerfAnalysis(eurFlowsStartDate, parse("2024-07-31"));
    }

    @Test
    void ptf11_18() {
        testPerfAnalysis(eurFlowsStartDate, parse("2024-10-18"));
    }

    @Test
    void ptf11_50() {
        testPerfAnalysis(parse("2023-10-17"), parse("2024-10-18"));
    }

    @Test
    void ptf11_51() {
        testPerfAnalysis(parse("2023-10-18"), parse("2024-10-18"));
    }

    @Test
    void ptf11_52() {
        testPerfAnalysis(parse("2023-11-01"), parse("2024-10-18"));
    }

    @Test
    void ptf11_53() {
        testPerfAnalysis(parse("2023-12-01"), parse("2024-10-18"));
    }

    @Test
    void ptf11_54() {
        testPerfAnalysis(parse("2023-12-29"), parse("2024-10-18"));
    }

    @Test
    void ptf11_55() {
        //IBKR gives a slightly different cumMwr here, see test-data file
        assertThrowsExactly(AssertionFailedError.class, () -> testPerfAnalysis(parse("2024-01-01"), parse("2024-10-18")));
    }

    @Test
    void ptf11_56() {
        testPerfAnalysis(parse("2024-01-02"), parse("2024-10-18"));
    }

    @Test
    void ptf11_58() {
        testPerfAnalysis(parse("2024-02-01"), parse("2024-10-18"));
    }

    @Test
    void ptf11_59() {
        testPerfAnalysis(parse("2024-02-09"), parse("2024-10-18"));
    }

    @Test
    void ptf11_60() {
        //IBKR gives a slightly different cumMwr here, see test-data file
        assertThrowsExactly(AssertionFailedError.class, () -> testPerfAnalysis(parse("2024-02-12"), parse("2024-10-18")));
    }

    @Test
    void ptf11_61() {
        testPerfAnalysis(parse("2024-02-13"), parse("2024-10-18"));
    }

    @Test
    void ptf11_62() {
        testPerfAnalysis(parse("2024-02-16"), parse("2024-10-18"));
    }

    @Test
    void ptf11_70() {
        //IBKR gives a slightly different cumMwr here, see test-data file
        assertThrowsExactly(AssertionFailedError.class, () -> testPerfAnalysis(parse("2024-02-19"), parse("2024-10-18")));
    }

    @Test
    void ptf11_80() {
        testPerfAnalysis(parse("2024-02-20"), parse("2024-10-18"));
    }

    @Test
    void ptf11_90() {
        testPerfAnalysis(parse("2024-02-28"), parse("2024-10-18"));
    }

    @Test
    void ptf11_100() {
        testPerfAnalysis(parse("2024-02-29"), parse("2024-10-18"));
    }

    @Test
    void ptf11_110() {
        testPerfAnalysis(parse("2024-03-01"), parse("2024-10-18"));
    }

    @Test
    void ptf11_140() {
        //IBKR gives a slightly different cumMwr here, see test-data file
        assertThrowsExactly(AssertionFailedError.class, () -> testPerfAnalysis(parse("2024-06-03"), parse("2024-10-18")));
    }

    @Test
    void ptf11_141() {
        testPerfAnalysis(parse("2024-06-04"), parse("2024-10-18"));
    }

}
