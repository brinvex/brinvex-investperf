package com.brinvex.investperf.internal;

import com.brinvex.investperf.api.Frequency;
import com.brinvex.investperf.api.Annualizer;
import com.brinvex.investperf.api.FlowTiming;
import com.brinvex.investperf.api.PerfAnalysis;
import com.brinvex.investperf.api.PerfAnalysisRequest;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceAnalyzer;
import com.brinvex.investperf.api.PerformanceCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.MwrCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.TwrCalculator;
import com.brinvex.investperf.internal.util.LimitedLinkedMap;
import com.brinvex.investperf.internal.util.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.brinvex.investperf.api.AnnualizationOption.ANNUALIZE_IF_OVER_ONE_YEAR;
import static com.brinvex.investperf.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.investperf.internal.util.DateUtil.maxDate;
import static com.brinvex.investperf.internal.util.DateUtil.minDate;
import static com.brinvex.investperf.internal.util.NullUtil.nullSafe;
import static com.brinvex.investperf.internal.util.CollectionUtil.rangeSafeHeadMap;
import static com.brinvex.investperf.internal.util.CollectionUtil.rangeSafeTailMap;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptySortedMap;

@SuppressWarnings("DuplicatedCode")
public class PerformanceAnalyzerImpl implements PerformanceAnalyzer {

    @SuppressWarnings("DataFlowIssue")
    @Override
    public SequencedCollection<PerfAnalysis> analyzePerformance(PerfAnalysisRequest req) {
        Frequency frequency = req.resultFrequency();
        LocalDate resultStartDateIncl = req.resultStartDateIncl();
        LocalDate resultEndDateIncl = req.resultEndDateIncl();
        FlowTiming twrFlowTiming = req.twrFlowTiming();
        FlowTiming mwrFlowTiming = req.mwrFlowTiming();
        boolean resultRatesInPct = req.resultRatesInPercent();
        int calcScale = req.calcScale();
        int resultRateScale = req.resultRateScale();
        int resultAmountScale = req.resultAmountScale();
        RoundingMode roundingMode = req.roundingMode();
        TwrCalculator twrCalculator = PerformanceCalculator.twrCalculator(req.twrCalculatorType());
        MwrCalculator mwrCalculator = PerformanceCalculator.mwrCalculator(req.mwrCalculatorType());
        boolean calculateTrailingAvgProfit1Y = req.calculateTrailingAvgProfit1Y();
        boolean calculateTrailingAvgFlow1Y = req.calculateTrailingAvgFlow1Y();
        boolean calculatePeriodIncome = req.calculatePeriodIncome();
        boolean calculateTrailingAvgIncome1Y = req.calculateTrailingAvgIncome1Y();
        boolean calculateTrailingTwr1Y = req.calculateTrailingTwr1Y();
        boolean calculateTrailingTwr2Y = req.calculateTrailingTwr2Y();
        boolean calculateTrailingTwr3Y = req.calculateTrailingTwr3Y();
        boolean calculateTrailingTwr4Y = req.calculateTrailingTwr4Y();
        boolean calculateTrailingTwr5Y = req.calculateTrailingTwr5Y();
        boolean calculateTrailingTwr10Y = req.calculateTrailingTwr10Y();
        boolean calculateYtdTwr = req.calculateYtdTwr();
        Function<LocalDate, BigDecimal> assetValues = req.assetValues();

        LocalDate calcStartDateIncl = minDate(maxDate(resultStartDateIncl, req.performanceMeasureStartDateIncl()), resultEndDateIncl.plusDays(1));
        LocalDate calcStartDateExcl = calcStartDateIncl.minusDays(1);
        LocalDate calcEndDateIncl = minDate(resultEndDateIncl, req.performanceMeasureEndDateIncl());
        LocalDate calcEndDateExcl = calcEndDateIncl.plusDays(1);
        boolean calcIsNeeded = !calcStartDateIncl.isAfter(calcEndDateIncl);

        SortedMap<LocalDate, BigDecimal> flows = req.flows().apply(calcStartDateIncl, calcEndDateIncl);
        if (flows == null) {
            flows = emptySortedMap();
        } else if (!flows.isEmpty()) {
            Entry<LocalDate, BigDecimal> firstFlow = flows.firstEntry();
            if (firstFlow.getKey().isBefore(calcStartDateIncl)) {
                throw new IllegalArgumentException("firstFlow must not be before calcStartDateIncl; %s, %s"
                        .formatted(firstFlow, calcStartDateExcl));
            }
            Entry<LocalDate, BigDecimal> lastFlow = flows.lastEntry();
            if (lastFlow.getKey().isAfter(calcEndDateIncl)) {
                throw new IllegalArgumentException("lastFlow must not be after calcEndDateIncl; %s, %s"
                        .formatted(lastFlow, calcEndDateIncl));
            }
        }
        SortedMap<LocalDate, BigDecimal> incomes = nullSafe(req.incomes(), _incomes -> _incomes.apply(calcStartDateIncl, calcEndDateIncl));
        if (incomes == null) {
            incomes = emptySortedMap();
        } else if (!incomes.isEmpty()) {
            Entry<LocalDate, BigDecimal> firstIncome = incomes.firstEntry();
            if (firstIncome.getKey().isBefore(calcStartDateIncl)) {
                throw new IllegalArgumentException("firstIncome must not be before calcStartDateIncl; %s, %s"
                        .formatted(firstIncome, calcStartDateExcl));
            }
            Entry<LocalDate, BigDecimal> lastIncome = incomes.lastEntry();
            if (lastIncome.getKey().isAfter(calcEndDateIncl)) {
                throw new IllegalArgumentException("lastIncome must not be after calcEndDateIncl; %s, %s"
                        .formatted(lastIncome, calcEndDateIncl));
            }
        }

        Annualizer annualizer = Annualizer.INSTANCE;
        SequencedMap<String, PerfAnalysis> results = new LinkedHashMap<>();

        {
            LocalDate periodStartDateIncl = resultStartDateIncl;
            while (periodStartDateIncl.isBefore(calcStartDateIncl)) {
                LocalDate periodEndDateIncl = minDate(frequency.adjustToEndDateIncl(periodStartDateIncl), calcStartDateIncl.minusDays(1));
                LocalDate periodEndDateExcl = periodEndDateIncl.plusDays(1);
                String periodCaption = frequency.caption(periodStartDateIncl);
                BigDecimal periodEndValueIncl;
                if (!calcIsNeeded || periodEndDateExcl.isBefore(calcStartDateIncl)) {
                    periodEndValueIncl = null;
                } else {
                    periodEndValueIncl = assetValues.apply(periodEndDateIncl);
                }
                results.put(periodCaption, PerfAnalysis.builder()
                        .periodCaption(periodCaption)
                        .periodStartDateIncl(periodStartDateIncl)
                        .periodEndDateIncl(periodEndDateIncl)
                        .periodEndAssetValueIncl(periodEndValueIncl)
                        .build());
                //For the next iteration
                periodStartDateIncl = periodEndDateExcl;
            }
        }
        if (calcIsNeeded) {
            SortedMap<LocalDate, BigDecimal> iterativeForwardFlows = flows;
            SortedMap<LocalDate, BigDecimal> iterativeForwardIncomes = incomes;
            int periodFrequencyPerYear = frequency.countPerYear();
            LimitedLinkedMap<LocalDate, BigDecimal> trailingProfits1Y = calculateTrailingAvgProfit1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingFlows1Y = calculateTrailingAvgFlow1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingIncomes1Y = calculateTrailingAvgIncome1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            List<Integer> trailingTwrYears = IntStream.of(1, 2, 3, 4, 5, 10)
                    .filter(years -> switch (years) {
                        case 1 -> calculateTrailingTwr1Y;
                        case 2 -> calculateTrailingTwr2Y;
                        case 3 -> calculateTrailingTwr3Y;
                        case 4 -> calculateTrailingTwr4Y;
                        case 5 -> calculateTrailingTwr5Y;
                        default -> calculateTrailingTwr10Y;
                    })
                    .boxed()
                    .toList();
            /* The latest periods' TWR factors, as many as the longest trailing window asked for spans. */
            LimitedLinkedMap<LocalDate, BigDecimal> latestTwrFactors = trailingTwrYears.isEmpty()
                    ? null : new LimitedLinkedMap<>(periodFrequencyPerYear * trailingTwrYears.getLast());
            BigDecimal ytdTwrFactor = ONE;
            int ytdYear = calcStartDateIncl.getYear();

            BigDecimal startValueExcl = assetValues.apply(calcStartDateExcl);
            if (startValueExcl == null) {
                throw new IllegalArgumentException("startValueExcl must not be null, missing assetValue for calcStartDateExcl=%s"
                        .formatted(calcStartDateExcl));
            }
            BigDecimal cumulTwrFactor = ONE;
            BigDecimal totalContribution = startValueExcl;
            BigDecimal totalProfit = ZERO;

            LocalDate periodStartDateIncl = calcStartDateIncl;
            while (!periodStartDateIncl.isAfter(calcEndDateIncl)) {
                LocalDate periodStartDateExcl = periodStartDateIncl.minusDays(1);
                LocalDate periodEndDateIncl = minDate(frequency.adjustToEndDateIncl(periodStartDateIncl), calcEndDateIncl);
                LocalDate periodEndDateExcl = periodEndDateIncl.plusDays(1);
                BigDecimal periodStartValueExcl = periodStartDateIncl.isEqual(calcStartDateIncl) ? startValueExcl : assetValues.apply(periodStartDateExcl);
                if (periodStartValueExcl == null) {
                    throw new IllegalArgumentException("periodStartValueExcl must not be null, missing assetValue for periodStartDateExcl=%s"
                            .formatted(periodStartDateExcl));
                }
                BigDecimal periodEndValueIncl = assetValues.apply(periodEndDateIncl);
                if (periodEndValueIncl == null) {
                    throw new IllegalArgumentException("periodEndValueIncl must not be null, missing assetValue for periodEndDateIncl=%s"
                            .formatted(periodEndDateIncl));
                }

                SortedMap<LocalDate, BigDecimal> periodFlows = rangeSafeHeadMap(iterativeForwardFlows, periodEndDateExcl);

                BigDecimal periodTwr;
                {
                    BigDecimal adjPeriodStartValueExcl = periodStartValueExcl;
                    SortedMap<LocalDate, BigDecimal> adjPeriodFlows = periodFlows;
                    if (!periodFlows.isEmpty()) {
                        if (twrFlowTiming == BEGINNING_OF_DAY) {
                            Entry<LocalDate, BigDecimal> firstFlowEntry = periodFlows.firstEntry();
                            LocalDate firstFlowDate = firstFlowEntry.getKey();
                            if (firstFlowDate.isEqual(periodStartDateIncl)) {
                                adjPeriodStartValueExcl = periodStartValueExcl.add(firstFlowEntry.getValue());
                                adjPeriodFlows = rangeSafeTailMap(flows, firstFlowDate.plusDays(1));
                            }
                        }
                    }
                    PerfCalcRequest periodPerfCalcReq;
                    if (adjPeriodStartValueExcl.compareTo(ZERO) == 0) {
                        if (adjPeriodFlows.isEmpty()) {
                            if (periodEndValueIncl.compareTo(ZERO) == 0) {
                                periodTwr = ZERO;
                            } else {
                                throw new IllegalArgumentException((
                                        "if periodStartValueExcl is zero and periodFlows is empty, then periodEndValueIncl must be zero; given: " +
                                        "periodEndValueIncl=%s, periodIncl=%s-%s, ")
                                        .formatted(periodEndValueIncl, periodStartDateIncl, periodEndDateIncl));
                            }
                        } else {
                            LocalDate adjPeriodStartDateIncl;
                            adjPeriodStartDateIncl = switch (twrFlowTiming) {
                                case BEGINNING_OF_DAY -> adjPeriodFlows.firstKey();
                                case END_OF_DAY -> adjPeriodFlows.firstKey().plusDays(1);
                            };
                            periodPerfCalcReq = PerfCalcRequest.builder()
                                    .startDateIncl(adjPeriodStartDateIncl)
                                    .endDateIncl(periodEndDateIncl)
                                    .startAssetValueExcl(periodStartValueExcl)
                                    .endAssetValueIncl(periodEndValueIncl)
                                    .flows(adjPeriodFlows)
                                    .assetValues(assetValues)
                                    .flowTiming(twrFlowTiming)
                                    .largeFlowRule(req.largeFlowRule())
                                    .annualization(DO_NOT_ANNUALIZE)
                                    .calcScale(calcScale)
                                    .resultScale(calcScale)
                                    .roundingMode(roundingMode)
                                    .build();
                            periodTwr = twrCalculator.calculateReturn(periodPerfCalcReq);
                        }
                    } else {
                        periodPerfCalcReq = PerfCalcRequest.builder()
                                .startDateIncl(periodStartDateIncl)
                                .endDateIncl(periodEndDateIncl)
                                .startAssetValueExcl(periodStartValueExcl)
                                .endAssetValueIncl(periodEndValueIncl)
                                .flows(periodFlows)
                                .assetValues(assetValues)
                                .flowTiming(twrFlowTiming)
                                .largeFlowRule(req.largeFlowRule())
                                .annualization(DO_NOT_ANNUALIZE)
                                .calcScale(calcScale)
                                .resultScale(calcScale)
                                .roundingMode(roundingMode)
                                .build();
                        periodTwr = twrCalculator.calculateReturn(periodPerfCalcReq);
                    }
                }

                BigDecimal periodTwrFactor = periodTwr.add(ONE);
                cumulTwrFactor = cumulTwrFactor.multiply(periodTwrFactor).setScale(calcScale, roundingMode);
                BigDecimal annTwrFactor = annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, cumulTwrFactor, calcStartDateIncl, periodEndDateIncl);

                BigDecimal cumulMwr;
                BigDecimal annMwr;
                if (req.calculateMwr()) {
                    if (startValueExcl.compareTo(ZERO) == 0) {
                        SortedMap<LocalDate, BigDecimal> backwardFlows = rangeSafeHeadMap(flows, periodEndDateExcl);
                        if (backwardFlows.isEmpty()) {
                            cumulMwr = ZERO;
                        } else {
                            cumulMwr = mwrCalculator.calculateReturn(PerfCalcRequest.builder()
                                    .startDateIncl(backwardFlows.firstKey())
                                    .endDateIncl(periodEndDateIncl)
                                    .startAssetValueExcl(startValueExcl)
                                    .endAssetValueIncl(periodEndValueIncl)
                                    .flows(flows)
                                    .assetValues(assetValues)
                                    .flowTiming(mwrFlowTiming)
                                    .annualization(DO_NOT_ANNUALIZE)
                                    .calcScale(calcScale)
                                    .resultScale(calcScale)
                                    .roundingMode(roundingMode)
                                    .build());
                        }
                    } else {
                        cumulMwr = mwrCalculator.calculateReturn(PerfCalcRequest.builder()
                                .startDateIncl(calcStartDateIncl)
                                .endDateIncl(periodEndDateIncl)
                                .startAssetValueExcl(startValueExcl)
                                .endAssetValueIncl(periodEndValueIncl)
                                .flows(flows)
                                .assetValues(assetValues)
                                .flowTiming(mwrFlowTiming)
                                .annualization(DO_NOT_ANNUALIZE)
                                .calcScale(calcScale)
                                .resultScale(calcScale)
                                .roundingMode(roundingMode)
                                .build());
                    }
                    annMwr = annualizer.annualizeReturn(ANNUALIZE_IF_OVER_ONE_YEAR, cumulMwr, calcStartDateIncl, periodEndDateIncl);
                } else {
                    cumulMwr = null;
                    annMwr = null;
                }

                BigDecimal periodFlowSum = periodFlows.values().stream().reduce(ZERO, BigDecimal::add);
                totalContribution = totalContribution.add(periodFlowSum);
                BigDecimal periodProfit = periodEndValueIncl.subtract(periodStartValueExcl).subtract(periodFlowSum);
                totalProfit = totalProfit.add(periodProfit);

                BigDecimal trailingAvgProfit1Y;
                if (calculateTrailingAvgProfit1Y) {
                    trailingProfits1Y.put(periodStartDateIncl, periodProfit);
                    trailingAvgProfit1Y = trailingProfits1Y.values()
                            .stream()
                            .reduce(ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(periodFrequencyPerYear), resultAmountScale, roundingMode);
                } else {
                    trailingAvgProfit1Y = null;
                }

                BigDecimal trailingAvgFlow1Y;
                if (calculateTrailingAvgFlow1Y) {
                    trailingFlows1Y.put(periodStartDateIncl, periodFlowSum);
                    trailingAvgFlow1Y = trailingFlows1Y.values()
                            .stream()
                            .reduce(ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(periodFrequencyPerYear), resultAmountScale, roundingMode);
                } else {
                    trailingAvgFlow1Y = null;
                }

                BigDecimal periodIncomeSum;
                BigDecimal trailingAvgIncome1Y;
                if (calculatePeriodIncome || calculateTrailingAvgIncome1Y) {
                    SortedMap<LocalDate, BigDecimal> periodIncomes = rangeSafeHeadMap(iterativeForwardIncomes, periodEndDateExcl);
                    periodIncomeSum = periodIncomes.values().stream().reduce(ZERO, BigDecimal::add);
                    if (calculateTrailingAvgIncome1Y) {
                        trailingIncomes1Y.put(periodStartDateIncl, periodIncomeSum);
                        trailingAvgIncome1Y = trailingIncomes1Y.values()
                                .stream()
                                .reduce(ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(periodFrequencyPerYear), resultAmountScale, roundingMode);
                    } else {
                        trailingAvgIncome1Y = null;
                    }
                } else {
                    periodIncomeSum = null;
                    trailingAvgIncome1Y = null;
                }

                if (calculateYtdTwr) {
                    if (periodStartDateIncl.getYear() != ytdYear) {
                        ytdYear = periodStartDateIncl.getYear();
                        ytdTwrFactor = ONE;
                    }
                    ytdTwrFactor = ytdTwrFactor.multiply(periodTwrFactor).setScale(calcScale, roundingMode);
                }

                Map<Integer, BigDecimal> trailTwrFactors;
                if (latestTwrFactors == null) {
                    trailTwrFactors = Map.of();
                } else {
                    latestTwrFactors.put(periodStartDateIncl, periodTwrFactor);
                    trailTwrFactors = trailingTwrFactors(
                            latestTwrFactors, trailingTwrYears, periodFrequencyPerYear, calcScale, roundingMode, annualizer);
                }

                String periodCaption = frequency.caption(periodStartDateIncl);
                results.put(periodCaption, PerfAnalysis.builder()
                        .periodStartDateIncl(periodStartDateIncl)
                        .periodEndDateIncl(periodEndDateIncl)
                        .periodCaption(periodCaption)
                        .periodStartAssetValueExcl(Num.setScale(periodStartValueExcl, resultAmountScale, roundingMode))
                        .periodEndAssetValueIncl(Num.setScale(periodEndValueIncl, resultAmountScale, roundingMode))
                        .periodFlow(Num.setScale(periodFlowSum, resultAmountScale, roundingMode))
                        .periodTwr(toPctAndScale(periodTwr, resultRatesInPct, resultRateScale, roundingMode))
                        .cumulativeTwr(toPctAndScale(cumulTwrFactor.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .annualizedTwr(toPctAndScale(annTwrFactor.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .ytdTwr(calculateYtdTwr ? toPctAndScale(ytdTwrFactor.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode) : null)
                        .cumulativeMwr(toPctAndScale(cumulMwr, resultRatesInPct, resultRateScale, roundingMode))
                        .annualizedMwr(toPctAndScale(annMwr, resultRatesInPct, resultRateScale, roundingMode))
                        .totalContribution(Num.setScale(totalContribution, resultAmountScale, roundingMode))
                        .periodProfit(Num.setScale(periodProfit, resultAmountScale, roundingMode))
                        .totalProfit(Num.setScale(totalProfit, resultAmountScale, roundingMode))
                        .trailingAvgProfit1Y(trailingAvgProfit1Y)
                        .trailingAvgFlow1Y(trailingAvgFlow1Y)
                        .periodIncome(Num.setScale(periodIncomeSum, resultAmountScale, roundingMode))
                        .trailingAvgIncome1Y(trailingAvgIncome1Y)
                        .trailingTwr1Y(growthFactorToRate(trailTwrFactors.get(1), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr2Y(growthFactorToRate(trailTwrFactors.get(2), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr3Y(growthFactorToRate(trailTwrFactors.get(3), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr4Y(growthFactorToRate(trailTwrFactors.get(4), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr5Y(growthFactorToRate(trailTwrFactors.get(5), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr10Y(growthFactorToRate(trailTwrFactors.get(10), resultRatesInPct, resultRateScale, roundingMode))
                        .build());

                //For the next iteration
                {
                    periodStartDateIncl = periodEndDateExcl;
                    iterativeForwardFlows = rangeSafeTailMap(iterativeForwardFlows, periodEndDateExcl);
                    if (calculatePeriodIncome || calculateTrailingAvgIncome1Y) {
                        iterativeForwardIncomes = rangeSafeTailMap(iterativeForwardIncomes, periodEndDateExcl);
                    }
                }
            }
        }
        {
            LocalDate periodStartDateIncl = maxDate(calcEndDateExcl, resultStartDateIncl);
            while (!periodStartDateIncl.isAfter(resultEndDateIncl)) {
                LocalDate periodEndDateIncl = minDate(frequency.adjustToEndDateIncl(periodStartDateIncl), resultEndDateIncl);
                String periodCaption = frequency.caption(periodStartDateIncl);
                results.putIfAbsent(periodCaption, PerfAnalysis.builder()
                        .periodStartDateIncl(periodStartDateIncl)
                        .periodEndDateIncl(periodEndDateIncl)
                        .periodCaption(periodCaption)
                        .build());
                //For the next iteration
                periodStartDateIncl = periodEndDateIncl.plusDays(1);
            }
        }
        return (SequencedCollection<PerfAnalysis>) results.values();
    }

    /**
     * The growth factor of each trailing window the periods seen so far fill, keyed by its length in
     * years and annualised over it; a window not yet filled is left out. The factors are taken from
     * the latest back, so each window's product goes on from the shorter one's.
     */
    private static Map<Integer, BigDecimal> trailingTwrFactors(
            SequencedMap<LocalDate, BigDecimal> periodTwrFactors,
            List<Integer> windowYears,
            int periodsPerYear,
            int calcScale,
            RoundingMode roundingMode,
            Annualizer annualizer
    ) {
        Map<Integer, BigDecimal> results = new HashMap<>();
        Iterator<BigDecimal> latestFirst = periodTwrFactors.sequencedValues().reversed().iterator();
        BigDecimal product = ONE;
        int periods = 0;
        for (int years : windowYears) {
            int windowPeriods = years * periodsPerYear;
            if (periodTwrFactors.size() < windowPeriods) {
                break;
            }
            for (; periods < windowPeriods; periods++) {
                product = product.multiply(latestFirst.next());
            }
            BigDecimal factor = product.setScale(calcScale, roundingMode);
            results.put(years, annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, factor, years));
        }
        return results;
    }

    private static BigDecimal growthFactorToRate(BigDecimal growthFactor, boolean toPercent, int scale, RoundingMode roundingMode) {
        return growthFactor == null ? null : toPctAndScale(growthFactor.subtract(ONE), toPercent, scale, roundingMode);
    }

    private static BigDecimal toPctAndScale(BigDecimal input, boolean toPercent, int scale, RoundingMode roundingMode) {
        if (input == null) {
            return null;
        }
        if (toPercent) {
            input = input.multiply(Num._100);
        }
        return input.setScale(scale, roundingMode);
    }

}
