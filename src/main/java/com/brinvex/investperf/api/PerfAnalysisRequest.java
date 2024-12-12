package com.brinvex.investperf.api;

import com.brinvex.fintypes.enu.Frequency;
import com.brinvex.fintypes.vo.DateAmount;
import com.brinvex.investperf.api.PerformanceCalculator.ModifiedDietzMwrCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.MwrCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.TrueTwrCalculator;
import com.brinvex.investperf.api.PerformanceCalculator.TwrCalculator;
import com.brinvex.investperf.internal.RequestSanitizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PerfAnalysisRequest {
    private final Frequency resultFrequency;
    private final LocalDate resultStartDateIncl;
    private final LocalDate resultEndDateIncl;
    private final LocalDate performanceMeasureStartDateIncl;
    private final LocalDate performanceMeasureEndDateIncl;
    private final Function<LocalDate, BigDecimal> assetValues;
    private final BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flows;
    private final FlowTiming twrFlowTiming;
    private final FlowTiming mwrFlowTiming;
    private final String twrCalculatorType;
    private final String mwrCalculatorType;
    private final int largeFlowLevelInPercent;
    private final boolean resultRatesInPercent;
    private final int calcScale;
    private final int resultRateScale;
    private final int resultAmountScale;
    private final RoundingMode roundingMode;
    private final boolean calculateMwr;
    private final boolean calculateTrailingAvgProfit1Y;
    private final boolean calculateTrailingAvgFlow1Y;
    private final boolean calculatePeriodIncome;
    private final boolean calculateTrailingAvgIncome1Y;
    private final BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> incomes;
    private final boolean calculateTrailingTwr1Y;
    private final boolean calculateTrailingTwr2Y;
    private final boolean calculateTrailingTwr3Y;
    private final boolean calculateTrailingTwr5Y;
    private final boolean calculateTrailingTwr10Y;

    @SuppressWarnings("ReplaceNullCheck")
    private PerfAnalysisRequest(
            Frequency resultFrequency,
            LocalDate resultStartDateIncl,
            LocalDate resultEndDateIncl,
            LocalDate performanceMeasureStartDateIncl,
            LocalDate performanceMeasureEndDateIncl,
            Function<LocalDate, BigDecimal> assetValuesProvider,
            Map<LocalDate, BigDecimal> assetValuesMap,
            Collection<DateAmount> assetValuesCollection,
            BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flowsProvider,
            Map<LocalDate, BigDecimal> flowsMap,
            Collection<DateAmount> flowsCollection,
            String twrCalculatorType,
            String mwrCalculatorType,
            FlowTiming twrFlowTiming,
            FlowTiming mwrFlowTiming,
            Integer largeFlowLevelInPercent,
            Boolean resultRatesInPercent,
            Integer calcScale,
            Integer resultRateScale,
            Integer resultAmountScale,
            RoundingMode roundingMode,
            Boolean calculateMwr,
            Boolean calculateTrailingAvgProfit1Y,
            Boolean calculateTrailingAvgFlow1Y,
            Boolean calculatePeriodIncome,
            Boolean calculateTrailingAvgIncome1Y,
            BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> incomesProvider,
            Map<LocalDate, BigDecimal> incomesMap,
            Collection<DateAmount> incomesCollection,
            Boolean calculateTrailingTwr1Y,
            Boolean calculateTrailingTwr2Y,
            Boolean calculateTrailingTwr3Y,
            Boolean calculateTrailingTwr5Y,
            Boolean calculateTrailingTwr10Y
    ) {
        if (resultStartDateIncl == null) {
            throw new IllegalArgumentException("resultStartDateIncl must not be null");
        }
        if (resultEndDateIncl == null) {
            throw new IllegalArgumentException("resultEndDateIncl must not be null");
        }
        if (resultStartDateIncl.isAfter(resultEndDateIncl)) {
            throw new IllegalArgumentException("resultStartDateIncl must be before resultEndDateIncl, given: %s, %s"
                    .formatted(resultStartDateIncl, resultEndDateIncl));
        }
        if (resultFrequency == Frequency.DAY) {
            throw new IllegalArgumentException("resultFrequency must not be DAY");
        }
        this.resultStartDateIncl = resultStartDateIncl;
        this.resultEndDateIncl = resultEndDateIncl;
        this.performanceMeasureStartDateIncl = performanceMeasureStartDateIncl == null ? resultStartDateIncl : performanceMeasureStartDateIncl;
        this.performanceMeasureEndDateIncl = performanceMeasureEndDateIncl == null ? resultEndDateIncl : performanceMeasureEndDateIncl;
        this.resultFrequency = resultFrequency == null ? Frequency.MONTH : resultFrequency;
        this.twrFlowTiming = twrFlowTiming == null ? FlowTiming.BEGINNING_OF_DAY : twrFlowTiming;
        this.mwrFlowTiming = mwrFlowTiming == null ? FlowTiming.BEGINNING_OF_DAY : mwrFlowTiming;
        this.twrCalculatorType = twrCalculatorType == null ? TrueTwrCalculator.class.getSimpleName() : twrCalculatorType;
        this.mwrCalculatorType = mwrCalculatorType == null ? ModifiedDietzMwrCalculator.class.getSimpleName() : mwrCalculatorType;
        this.resultRatesInPercent = resultRatesInPercent != null && resultRatesInPercent;
        this.largeFlowLevelInPercent = largeFlowLevelInPercent == null ? 5 : largeFlowLevelInPercent;
        this.calcScale = calcScale == null ? 20 : calcScale;
        this.resultRateScale = resultRateScale == null ? 6 : resultRateScale;
        this.resultAmountScale = resultAmountScale == null ? 2 : resultAmountScale;
        this.roundingMode = roundingMode == null ? RoundingMode.HALF_UP : roundingMode;
        this.calculateMwr = calculateMwr != null && calculateMwr;
        this.calculateTrailingAvgProfit1Y = calculateTrailingAvgProfit1Y != null && calculateTrailingAvgProfit1Y;
        this.calculateTrailingAvgFlow1Y = calculateTrailingAvgFlow1Y != null && calculateTrailingAvgFlow1Y;
        this.calculatePeriodIncome = calculatePeriodIncome != null && calculatePeriodIncome;
        this.calculateTrailingAvgIncome1Y = calculateTrailingAvgIncome1Y != null && calculateTrailingAvgIncome1Y;
        this.calculateTrailingTwr1Y = calculateTrailingTwr1Y != null && calculateTrailingTwr1Y;
        this.calculateTrailingTwr2Y = calculateTrailingTwr2Y != null && calculateTrailingTwr2Y;
        this.calculateTrailingTwr3Y = calculateTrailingTwr3Y != null && calculateTrailingTwr3Y;
        this.calculateTrailingTwr5Y = calculateTrailingTwr5Y != null && calculateTrailingTwr5Y;
        this.calculateTrailingTwr10Y = calculateTrailingTwr10Y != null && calculateTrailingTwr10Y;

        LocalDate calcStartDateIncl = this.resultStartDateIncl.isAfter(this.performanceMeasureStartDateIncl) ? this.resultStartDateIncl : this.performanceMeasureStartDateIncl;
        LocalDate calcEndDateIncl = this.resultEndDateIncl.isBefore(this.performanceMeasureEndDateIncl) ? this.resultEndDateIncl : this.performanceMeasureEndDateIncl;

        this.assetValues = RequestSanitizer.sanitizeAssetValues(
                assetValuesProvider,
                assetValuesMap,
                assetValuesCollection,
                calcStartDateIncl,
                calcEndDateIncl
        );

        if (flowsProvider != null) {
            this.flows = flowsProvider;
        } else {
            this.flows = (_, _) -> RequestSanitizer.sanitizeFlows(
                    flowsMap,
                    flowsCollection,
                    calcStartDateIncl,
                    calcEndDateIncl
            );
        }

        if (this.calculatePeriodIncome || this.calculateTrailingAvgIncome1Y) {
            if (incomesProvider == null && incomesMap == null && incomesCollection == null) {
                throw new IllegalArgumentException((
                        "if calculatePeriodIncome or calculateTrailingAvgIncome1Y is true, then incomes must not be null, given: %s, %s")
                        .formatted(this.calculatePeriodIncome, this.calculateTrailingAvgIncome1Y)
                );
            }
            if (incomesProvider != null) {
                this.incomes = incomesProvider;
            } else {
                this.incomes = (_, _) -> RequestSanitizer.sanitizeFlows(
                        incomesMap,
                        incomesCollection,
                        calcStartDateIncl,
                        calcEndDateIncl
                );
            }
        } else {
            this.incomes = null;
        }
    }

    public static PerfAnalysisRequestBuilder builder() {
        return new PerfAnalysisRequestBuilder();
    }

    public Frequency resultFrequency() {
        return this.resultFrequency;
    }

    public LocalDate resultStartDateIncl() {
        return this.resultStartDateIncl;
    }

    public LocalDate resultEndDateIncl() {
        return this.resultEndDateIncl;
    }

    public LocalDate performanceMeasureStartDateIncl() {
        return this.performanceMeasureStartDateIncl;
    }

    public LocalDate performanceMeasureEndDateIncl() {
        return this.performanceMeasureEndDateIncl;
    }

    public Function<LocalDate, BigDecimal> assetValues() {
        return this.assetValues;
    }

    public BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flows() {
        return this.flows;
    }

    public FlowTiming twrFlowTiming() {
        return this.twrFlowTiming;
    }

    public FlowTiming mwrFlowTiming() {
        return this.mwrFlowTiming;
    }

    public String twrCalculatorType() {
        return this.twrCalculatorType;
    }

    public String mwrCalculatorType() {
        return this.mwrCalculatorType;
    }

    public int largeFlowLevelInPercent() {
        return this.largeFlowLevelInPercent;
    }

    public boolean resultRatesInPercent() {
        return this.resultRatesInPercent;
    }

    public int calcScale() {
        return this.calcScale;
    }

    public int resultRateScale() {
        return this.resultRateScale;
    }

    public int resultAmountScale() {
        return this.resultAmountScale;
    }

    public RoundingMode roundingMode() {
        return this.roundingMode;
    }

    public boolean calculateMwr() {
        return this.calculateMwr;
    }

    public boolean calculateTrailingAvgProfit1Y() {
        return this.calculateTrailingAvgProfit1Y;
    }

    public boolean calculateTrailingAvgFlow1Y() {
        return this.calculateTrailingAvgFlow1Y;
    }

    public boolean calculatePeriodIncome() {
        return this.calculatePeriodIncome;
    }

    public boolean calculateTrailingAvgIncome1Y() {
        return this.calculateTrailingAvgIncome1Y;
    }

    public BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> incomes() {
        return this.incomes;
    }

    public boolean calculateTrailingTwr1Y() {
        return this.calculateTrailingTwr1Y;
    }

    public boolean calculateTrailingTwr2Y() {
        return this.calculateTrailingTwr2Y;
    }

    public boolean calculateTrailingTwr3Y() {
        return this.calculateTrailingTwr3Y;
    }

    public boolean calculateTrailingTwr5Y() {
        return this.calculateTrailingTwr5Y;
    }

    public boolean calculateTrailingTwr10Y() {
        return this.calculateTrailingTwr10Y;
    }

    public static class PerfAnalysisRequestBuilder {
        private Frequency resultFrequency;
        private LocalDate resultStartDateIncl;
        private LocalDate resultEndDateIncl;
        private LocalDate performanceMeasureStartDateIncl;
        private LocalDate performanceMeasureEndDateIncl;
        private Function<LocalDate, BigDecimal> assetValuesProvider;
        private Map<LocalDate, BigDecimal> assetValuesMap;
        private Collection<DateAmount> assetValuesCollection;
        private BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flowsProvider;
        private Map<LocalDate, BigDecimal> flowsMap;
        private Collection<DateAmount> flowsCollection;
        private BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> incomesProvider;
        private Map<LocalDate, BigDecimal> incomesMap;
        private Collection<DateAmount> incomesCollection;
        private FlowTiming twrFlowTiming;
        private FlowTiming mwrFlowTiming;
        private String twrCalculatorType;
        private String mwrCalculatorType;
        private Integer largeFlowLevelInPercent;
        private Boolean resultRatesInPercent;
        private Integer calcScale;
        private Integer resultRateScale;
        private Integer resultAmountScale;
        private RoundingMode roundingMode;
        private Boolean calculateMwr;
        private Boolean calculateTrailingAvgProfit1Y;
        private Boolean calculateTrailingAvgFlow1Y;
        private Boolean calculatePeriodIncome;
        private Boolean calculateTrailingAvgIncome1Y;
        private Boolean calculateTrailingTwr1Y;
        private Boolean calculateTrailingTwr2Y;
        private Boolean calculateTrailingTwr3Y;
        private Boolean calculateTrailingTwr5Y;
        private Boolean calculateTrailingTwr10Y;

        private PerfAnalysisRequestBuilder() {
        }

        public PerfAnalysisRequestBuilder flowTiming(FlowTiming flowTiming) {
            this.twrFlowTiming = flowTiming;
            this.mwrFlowTiming = flowTiming;
            return this;
        }

        public PerfAnalysisRequestBuilder resultScale(int scale) {
            this.resultRateScale = scale;
            this.resultAmountScale = scale;
            return this;
        }

        public PerfAnalysisRequestBuilder assetValues(Function<LocalDate, BigDecimal> assetValues) {
            this.assetValuesProvider = assetValues;
            this.assetValuesMap = null;
            this.assetValuesCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder assetValues(Map<LocalDate, BigDecimal> assetValues) {
            this.assetValuesProvider = null;
            this.assetValuesMap = assetValues;
            this.assetValuesCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder assetValues(Collection<DateAmount> assetValues) {
            this.assetValuesProvider = null;
            this.assetValuesMap = null;
            this.assetValuesCollection = assetValues;
            return this;
        }

        public PerfAnalysisRequestBuilder flows(BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flows) {
            this.flowsProvider = flows;
            this.flowsMap = null;
            this.flowsCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder flows(Map<LocalDate, BigDecimal> flows) {
            this.flowsProvider = null;
            this.flowsMap = flows;
            this.flowsCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder flows(Collection<DateAmount> flows) {
            this.flowsProvider = null;
            this.flowsMap = null;
            this.flowsCollection = flows;
            return this;
        }

        public PerfAnalysisRequestBuilder incomes(BiFunction<LocalDate, LocalDate, SortedMap<LocalDate, BigDecimal>> flows) {
            this.incomesProvider = flows;
            this.incomesMap = null;
            this.incomesCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder incomes(Map<LocalDate, BigDecimal> incomes) {
            this.incomesProvider = null;
            this.incomesMap = incomes;
            this.incomesCollection = null;
            return this;
        }

        public PerfAnalysisRequestBuilder incomes(Collection<DateAmount> incomes) {
            this.incomesProvider = null;
            this.incomesMap = null;
            this.incomesCollection = incomes;
            return this;
        }

        public PerfAnalysisRequest build() {
            return new PerfAnalysisRequest(
                    resultFrequency,
                    resultStartDateIncl,
                    resultEndDateIncl,
                    performanceMeasureStartDateIncl,
                    performanceMeasureEndDateIncl,
                    assetValuesProvider,
                    assetValuesMap,
                    assetValuesCollection,
                    flowsProvider,
                    flowsMap,
                    flowsCollection,
                    twrCalculatorType,
                    mwrCalculatorType,
                    twrFlowTiming,
                    mwrFlowTiming,
                    largeFlowLevelInPercent,
                    resultRatesInPercent,
                    calcScale,
                    resultRateScale,
                    resultAmountScale,
                    roundingMode,
                    calculateMwr,
                    calculateTrailingAvgProfit1Y,
                    calculateTrailingAvgFlow1Y,
                    calculatePeriodIncome,
                    calculateTrailingAvgIncome1Y,
                    incomesProvider,
                    incomesMap,
                    incomesCollection,
                    calculateTrailingTwr1Y,
                    calculateTrailingTwr2Y,
                    calculateTrailingTwr3Y,
                    calculateTrailingTwr5Y,
                    calculateTrailingTwr10Y
            );
        }

        public PerfAnalysisRequestBuilder resultFrequency(Frequency resultFrequency) {
            this.resultFrequency = resultFrequency;
            return this;
        }

        public PerfAnalysisRequestBuilder resultStartDateIncl(LocalDate resultStartDateIncl) {
            this.resultStartDateIncl = resultStartDateIncl;
            return this;
        }

        public PerfAnalysisRequestBuilder resultEndDateIncl(LocalDate resultEndDateIncl) {
            this.resultEndDateIncl = resultEndDateIncl;
            return this;
        }

        public PerfAnalysisRequestBuilder performanceMeasureStartDateIncl(LocalDate performanceMeasureStartDateIncl) {
            this.performanceMeasureStartDateIncl = performanceMeasureStartDateIncl;
            return this;
        }

        public PerfAnalysisRequestBuilder performanceMeasureEndDateIncl(LocalDate performanceMeasureEndDateIncl) {
            this.performanceMeasureEndDateIncl = performanceMeasureEndDateIncl;
            return this;
        }

        public PerfAnalysisRequestBuilder twrFlowTiming(FlowTiming twrFlowTiming) {
            this.twrFlowTiming = twrFlowTiming;
            return this;
        }

        public PerfAnalysisRequestBuilder mwrFlowTiming(FlowTiming mwrFlowTiming) {
            this.mwrFlowTiming = mwrFlowTiming;
            return this;
        }

        public PerfAnalysisRequestBuilder twrCalculatorType(Class<? extends TwrCalculator> twrCalculatorType) {
            this.twrCalculatorType = twrCalculatorType == null ? null : twrCalculatorType.getSimpleName();
            return this;
        }

        public PerfAnalysisRequestBuilder mwrCalculatorType(Class<? extends MwrCalculator> mwrCalculatorType) {
            this.mwrCalculatorType = mwrCalculatorType == null ? null : mwrCalculatorType.getSimpleName();
            return this;
        }

        public PerfAnalysisRequestBuilder twrCalculatorType(String twrCalculatorType) {
            this.twrCalculatorType = twrCalculatorType;
            return this;
        }

        public PerfAnalysisRequestBuilder mwrCalculatorType(String mwrCalculatorType) {
            this.mwrCalculatorType = mwrCalculatorType;
            return this;
        }

        public PerfAnalysisRequestBuilder largeFlowLevelInPercent(Integer largeFlowLevelInPercent) {
            this.largeFlowLevelInPercent = largeFlowLevelInPercent;
            return this;
        }

        public PerfAnalysisRequestBuilder resultRatesInPercent(Boolean resultRatesInPercent) {
            this.resultRatesInPercent = resultRatesInPercent;
            return this;
        }

        public PerfAnalysisRequestBuilder calcScale(Integer calcScale) {
            this.calcScale = calcScale;
            return this;
        }

        public PerfAnalysisRequestBuilder resultRateScale(Integer resultRateScale) {
            this.resultRateScale = resultRateScale;
            return this;
        }

        public PerfAnalysisRequestBuilder resultAmountScale(Integer resultAmountScale) {
            this.resultAmountScale = resultAmountScale;
            return this;
        }

        public PerfAnalysisRequestBuilder roundingMode(RoundingMode roundingMode) {
            this.roundingMode = roundingMode;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateMwr(Boolean calculateMwr) {
            this.calculateMwr = calculateMwr;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingAvgProfit1Y(Boolean calculateTrailingAvgProfit1Y) {
            this.calculateTrailingAvgProfit1Y = calculateTrailingAvgProfit1Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingAvgFlow1Y(Boolean calculateTrailingAvgFlow1Y) {
            this.calculateTrailingAvgFlow1Y = calculateTrailingAvgFlow1Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculatePeriodIncome(Boolean calculatePeriodIncome) {
            this.calculatePeriodIncome = calculatePeriodIncome;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingAvgIncome1Y(Boolean calculateTrailingAvgIncome1Y) {
            this.calculateTrailingAvgIncome1Y = calculateTrailingAvgIncome1Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingTwr1Y(Boolean calculateTrailingTwr1Y) {
            this.calculateTrailingTwr1Y = calculateTrailingTwr1Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingTwr2Y(Boolean calculateTrailingTwr2Y) {
            this.calculateTrailingTwr2Y = calculateTrailingTwr2Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingTwr3Y(Boolean calculateTrailingTwr3Y) {
            this.calculateTrailingTwr3Y = calculateTrailingTwr3Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingTwr5Y(Boolean calculateTrailingTwr5Y) {
            this.calculateTrailingTwr5Y = calculateTrailingTwr5Y;
            return this;
        }

        public PerfAnalysisRequestBuilder calculateTrailingTwr10Y(Boolean calculateTrailingTwr10Y) {
            this.calculateTrailingTwr10Y = calculateTrailingTwr10Y;
            return this;
        }
    }
}
