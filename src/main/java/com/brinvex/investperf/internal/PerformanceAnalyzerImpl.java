package com.brinvex.investperf.internal;

import com.brinvex.fintypes.enu.Frequency;
import com.brinvex.investperf.api.Annualizer;
import com.brinvex.investperf.api.FlowTiming;
import com.brinvex.investperf.api.PerfAnalysis;
import com.brinvex.investperf.api.PerfAnalysisRequest;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceAnalyzer;
import com.brinvex.investperf.api.PerformanceCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.MwrCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.TwrCalculator;
import com.brinvex.java.LimitedLinkedMap;
import com.brinvex.java.Num;
import com.brinvex.java.validation.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.function.Function;

import static com.brinvex.investperf.api.AnnualizationOption.ANNUALIZE_IF_OVER_ONE_YEAR;
import static com.brinvex.investperf.api.AnnualizationOption.DO_NOT_ANNUALIZE;
import static com.brinvex.investperf.api.FlowTiming.BEGINNING_OF_DAY;
import static com.brinvex.java.DateUtil.maxDate;
import static com.brinvex.java.DateUtil.minDate;
import static com.brinvex.java.NullUtil.nullSafe;
import static com.brinvex.java.collection.CollectionUtil.rangeSafeHeadMap;
import static com.brinvex.java.collection.CollectionUtil.rangeSafeTailMap;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptySortedMap;
import static java.util.Objects.requireNonNull;

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
        boolean calculateTrailingTwr5Y = req.calculateTrailingTwr5Y();
        boolean calculateTrailingTwr10Y = req.calculateTrailingTwr10Y();
        Function<LocalDate, BigDecimal> assetValues = req.assetValues();

        LocalDate calcStartDateIncl = maxDate(resultStartDateIncl, req.performanceMeasureStartDateIncl());
        LocalDate calcStartDateExcl = calcStartDateIncl.minusDays(1);
        LocalDate calcEndDateIncl = minDate(resultEndDateIncl, req.performanceMeasureEndDateIncl());
        LocalDate calcEndDateExcl = calcEndDateIncl.plusDays(1);

        SortedMap<LocalDate, BigDecimal> flows = req.flows().apply(calcStartDateIncl, calcEndDateIncl);
        if (flows == null) {
            flows = emptySortedMap();
        } else if (!flows.isEmpty()) {
            Entry<LocalDate, BigDecimal> firstFlow = flows.firstEntry();
            Assert.isTrue(!firstFlow.getKey().isBefore(calcStartDateIncl),
                    () -> "firstFlow must not be before calcStartDateIncl; %s, %s".formatted(firstFlow, calcStartDateExcl));
            Entry<LocalDate, BigDecimal> lastFlow = flows.lastEntry();
            Assert.isTrue(!lastFlow.getKey().isAfter(calcEndDateIncl),
                    () -> "lastFlow must not be after calcEndDateIncl; %s, %s".formatted(lastFlow, calcEndDateIncl));
        }
        SortedMap<LocalDate, BigDecimal> incomes = nullSafe(req.incomes(), _incomes -> _incomes.apply(calcStartDateIncl, calcEndDateIncl));
        if (incomes == null) {
            incomes = emptySortedMap();
        } else if (!incomes.isEmpty()) {
            Entry<LocalDate, BigDecimal> firstIncome = incomes.firstEntry();
            Assert.isTrue(!firstIncome.getKey().isBefore(calcStartDateIncl),
                    () -> "firstIncome must not be before calcStartDateIncl; %s, %s".formatted(firstIncome, calcStartDateExcl));
            Entry<LocalDate, BigDecimal> lastIncome = incomes.lastEntry();
            Assert.isTrue(!lastIncome.getKey().isAfter(calcEndDateIncl),
                    () -> "lastIncome must not be after calcEndDateIncl; %s, %s".formatted(lastIncome, calcEndDateIncl));
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
                if (periodEndDateExcl.isBefore(calcStartDateIncl)) {
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
        if (!calcStartDateIncl.isAfter(calcEndDateIncl)) {
            SortedMap<LocalDate, BigDecimal> iterativeForwardFlows = flows;
            SortedMap<LocalDate, BigDecimal> iterativeForwardIncomes = incomes;
            int periodFrequencyPerYear = frequency.countPerYear();
            int periodFrequencyPerYears2 = periodFrequencyPerYear * 2;
            int periodFrequencyPerYears3 = periodFrequencyPerYear * 3;
            int periodFrequencyPerYears5 = periodFrequencyPerYear * 5;
            int periodFrequencyPerYears10 = periodFrequencyPerYear * 10;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingProfits1Y = calculateTrailingAvgProfit1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingFlows1Y = calculateTrailingAvgFlow1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingIncomes1Y = calculateTrailingAvgIncome1Y ? new LimitedLinkedMap<>(periodFrequencyPerYear) : null;
            LimitedLinkedMap<LocalDate, BigDecimal> trailingTwrFactors1Y = new LimitedLinkedMap<>(periodFrequencyPerYear);
            LimitedLinkedMap<LocalDate, BigDecimal> trailingTwrFactors2Y = new LimitedLinkedMap<>(periodFrequencyPerYears2);
            LimitedLinkedMap<LocalDate, BigDecimal> trailingTwrFactors3Y = new LimitedLinkedMap<>(periodFrequencyPerYears3);
            LimitedLinkedMap<LocalDate, BigDecimal> trailingTwrFactors5Y = new LimitedLinkedMap<>(periodFrequencyPerYears5);
            LimitedLinkedMap<LocalDate, BigDecimal> trailingTwrFactors10Y = new LimitedLinkedMap<>(periodFrequencyPerYears10);

            BigDecimal startValueExcl = assetValues.apply(calcStartDateExcl);
            if (startValueExcl == null) {
                throw new IllegalStateException("startValueExcl must not be null, missing assetValue for calcStartDateExcl=%s"
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
                requireNonNull(periodStartValueExcl, () -> "periodStartValueExcl must not be null, missing assetValue for periodStartDateExcl=%s"
                        .formatted(periodStartDateExcl));
                BigDecimal periodEndValueIncl = assetValues.apply(periodEndDateIncl);
                requireNonNull(periodEndValueIncl, () -> "periodEndValueIncl must not be null, missing assetValue for periodEndDateIncl=%s"
                        .formatted(periodEndDateIncl));

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

                BigDecimal trailTwrFactor1Y = null;
                BigDecimal trailTwrFactor2Y = null;
                BigDecimal trailTwrFactor3Y = null;
                BigDecimal trailTwrFactor5Y = null;
                BigDecimal trailTwrFactor10Y = null;
                if (calculateTrailingTwr1Y || calculateTrailingTwr2Y || calculateTrailingTwr3Y || calculateTrailingTwr5Y || calculateTrailingTwr10Y) {
                    trailingTwrFactors1Y.put(periodStartDateIncl, periodTwrFactor);
                    if (trailingTwrFactors1Y.size() >= periodFrequencyPerYear) {
                        trailTwrFactor1Y = trailingTwrFactors1Y
                                .values()
                                .stream()
                                .reduce(ONE, BigDecimal::multiply)
                                .setScale(calcScale, roundingMode);
                    }
                    if (calculateTrailingTwr2Y || calculateTrailingTwr3Y || calculateTrailingTwr5Y || calculateTrailingTwr10Y) {
                        trailingTwrFactors2Y.put(periodStartDateIncl, periodTwrFactor);
                        if (trailingTwrFactors2Y.size() >= periodFrequencyPerYears2) {
                            assert trailTwrFactor1Y != null;
                            trailTwrFactor2Y = trailTwrFactor1Y.multiply(trailingTwrFactors2Y
                                            .reversed()
                                            .values()
                                            .stream()
                                            .skip(periodFrequencyPerYear)
                                            .reduce(ONE, BigDecimal::multiply))
                                    .setScale(calcScale, roundingMode);
                        }
                        if (calculateTrailingTwr3Y || calculateTrailingTwr5Y || calculateTrailingTwr10Y) {
                            trailingTwrFactors3Y.put(periodStartDateIncl, periodTwrFactor);
                            if (trailingTwrFactors3Y.size() >= periodFrequencyPerYears3) {
                                assert trailTwrFactor2Y != null;
                                trailTwrFactor3Y = trailTwrFactor2Y.multiply(trailingTwrFactors3Y
                                                .reversed()
                                                .values()
                                                .stream()
                                                .skip(periodFrequencyPerYears2)
                                                .reduce(ONE, BigDecimal::multiply))
                                        .setScale(calcScale, roundingMode);
                            }
                            if (calculateTrailingTwr5Y || calculateTrailingTwr10Y) {
                                trailingTwrFactors5Y.put(periodStartDateIncl, periodTwrFactor);
                                if (trailingTwrFactors5Y.size() >= periodFrequencyPerYears5) {
                                    assert trailTwrFactor3Y != null;
                                    trailTwrFactor5Y = trailTwrFactor3Y.multiply(trailingTwrFactors5Y
                                                    .reversed()
                                                    .values()
                                                    .stream()
                                                    .skip(periodFrequencyPerYears3)
                                                    .reduce(ONE, BigDecimal::multiply))
                                            .setScale(calcScale, roundingMode);
                                }
                                if (calculateTrailingTwr10Y) {
                                    trailingTwrFactors10Y.put(periodStartDateIncl, periodTwrFactor);
                                    if (trailingTwrFactors10Y.size() >= periodFrequencyPerYears10) {
                                        assert trailTwrFactor5Y != null;
                                        trailTwrFactor10Y = trailTwrFactor5Y.multiply(trailingTwrFactors10Y
                                                        .reversed()
                                                        .values()
                                                        .stream()
                                                        .skip(periodFrequencyPerYears5)
                                                        .reduce(ONE, BigDecimal::multiply))
                                                .setScale(calcScale, roundingMode);
                                    }
                                    trailTwrFactor10Y = trailTwrFactor10Y == null ? null : annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, trailTwrFactor10Y, 10);
                                }
                                trailTwrFactor5Y = trailTwrFactor5Y == null ? null : annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, trailTwrFactor5Y, 5);
                            }
                            trailTwrFactor3Y = trailTwrFactor3Y == null ? null : annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, trailTwrFactor3Y, 3);
                        }
                        trailTwrFactor2Y = trailTwrFactor2Y == null ? null : annualizer.annualizeGrowthFactor(ANNUALIZE_IF_OVER_ONE_YEAR, trailTwrFactor2Y, 2);
                    }
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
                        .cumulativeMwr(toPctAndScale(cumulMwr, resultRatesInPct, resultRateScale, roundingMode))
                        .annualizedMwr(toPctAndScale(annMwr, resultRatesInPct, resultRateScale, roundingMode))
                        .totalContribution(Num.setScale(totalContribution, resultAmountScale, roundingMode))
                        .periodProfit(Num.setScale(periodProfit, resultAmountScale, roundingMode))
                        .totalProfit(Num.setScale(totalProfit, resultAmountScale, roundingMode))
                        .trailingAvgProfit1Y(trailingAvgProfit1Y)
                        .trailingAvgFlow1Y(trailingAvgFlow1Y)
                        .periodIncome(Num.setScale(periodIncomeSum, resultAmountScale, roundingMode))
                        .trailingAvgIncome1Y(trailingAvgIncome1Y)
                        .trailingTwr1Y(toPctAndScale(trailTwrFactor1Y == null ? null : trailTwrFactor1Y.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr2Y(toPctAndScale(trailTwrFactor2Y == null ? null : trailTwrFactor2Y.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr3Y(toPctAndScale(trailTwrFactor3Y == null ? null : trailTwrFactor3Y.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr5Y(toPctAndScale(trailTwrFactor5Y == null ? null : trailTwrFactor5Y.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
                        .trailingTwr10Y(toPctAndScale(trailTwrFactor10Y == null ? null : trailTwrFactor10Y.subtract(ONE), resultRatesInPct, resultRateScale, roundingMode))
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
            LocalDate periodStartDateIncl = calcEndDateExcl;
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
