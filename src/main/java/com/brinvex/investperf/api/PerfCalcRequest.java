package com.brinvex.investperf.api;

import com.brinvex.fintypes.vo.DateAmount;
import com.brinvex.investperf.internal.RequestSanitizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.unmodifiableSortedMap;

public final class PerfCalcRequest {
    private final LocalDate startDateIncl;
    private final LocalDate endDateIncl;
    private final BigDecimal startAssetValueExcl;
    private final BigDecimal endAssetValueIncl;
    private final Function<LocalDate, BigDecimal> assetValues;
    private final SortedMap<LocalDate, BigDecimal> flows;
    private final int largeFlowLevelInPercent;
    private final FlowTiming flowTiming;
    private final AnnualizationOption annualization;
    private final boolean resultInPercent;
    private final int calcScale;
    private final int resultScale;
    private final RoundingMode roundingMode;

    private PerfCalcRequest(
            LocalDate startDateIncl,
            LocalDate endDateIncl,
            BigDecimal startAssetValueExcl,
            BigDecimal endAssetValueIncl,
            Function<LocalDate, BigDecimal> assetValuesProvider,
            Map<LocalDate, BigDecimal> assetValuesMap,
            Collection<DateAmount> assetValuesCollection,
            Map<LocalDate, BigDecimal> flowsMap,
            Collection<DateAmount> flowsCollection,
            Integer largeFlowLevelInPercent,
            FlowTiming flowTiming,
            AnnualizationOption annualization,
            Boolean resultInPercent,
            Integer calcScale,
            Integer resultScale,
            RoundingMode roundingMode
    ) {
        if (startDateIncl == null) {
            throw new IllegalArgumentException("startDateIncl must not be null");
        }
        if (endDateIncl == null) {
            throw new IllegalArgumentException("endDateIncl must not be null");
        }
        if (startDateIncl.isAfter(endDateIncl)) {
            throw new IllegalArgumentException("startDateIncl must be before endDateIncl, given: %s, %s"
                    .formatted(startDateIncl, endDateIncl));
        }
        if (startAssetValueExcl == null) {
            throw new IllegalArgumentException("startAssetValueExcl must not be null");
        }
        if (startAssetValueExcl.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("startAssetValueExcl must be greater than or equal to zero");
        }
        if (endAssetValueIncl == null) {
            throw new IllegalArgumentException("endAssetValueIncl must not be null");
        }
        if (endAssetValueIncl.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("endAssetValueIncl must be greater than or equal to zero");
        }
        this.startAssetValueExcl = startAssetValueExcl;
        this.endAssetValueIncl = endAssetValueIncl;
        this.startDateIncl = startDateIncl;
        this.endDateIncl = endDateIncl;
        this.largeFlowLevelInPercent = largeFlowLevelInPercent == null ? 5 : largeFlowLevelInPercent;
        this.flowTiming = flowTiming == null ? FlowTiming.BEGINNING_OF_DAY : flowTiming;
        this.annualization = annualization == null ? AnnualizationOption.DO_NOT_ANNUALIZE : annualization;
        this.resultInPercent = resultInPercent != null && resultInPercent;
        this.calcScale = calcScale == null ? 20 : calcScale;
        this.resultScale = resultScale == null ? 6 : resultScale;
        this.roundingMode = roundingMode == null ? RoundingMode.HALF_UP : roundingMode;

        this.assetValues = RequestSanitizer.sanitizeAssetValues(
                assetValuesProvider,
                assetValuesMap,
                assetValuesCollection,
                startDateIncl,
                endDateIncl
        );

        this.flows = unmodifiableSortedMap(RequestSanitizer.sanitizeFlows(
                flowsMap,
                flowsCollection,
                startDateIncl,
                endDateIncl
        ));
    }


    public static PerfCalcRequestBuilder builder() {
        return new PerfCalcRequestBuilder();
    }

    @SuppressWarnings("DuplicatedCode")
    public PerfCalcRequestBuilder toBuilder() {
        PerfCalcRequestBuilder builder = new PerfCalcRequestBuilder();
        builder.startDateIncl = startDateIncl;
        builder.endDateIncl = endDateIncl;
        builder.startAssetValueExcl = startAssetValueExcl;
        builder.endAssetValueIncl = endAssetValueIncl;
        builder.largeFlowLevelInPercent = largeFlowLevelInPercent;
        builder.flowTiming = flowTiming;
        builder.annualization = annualization;
        builder.resultInPercent = resultInPercent;
        builder.calcScale = calcScale;
        builder.resultScale = resultScale;
        builder.assetValuesProvider = assetValues;
        builder.flowsMap = flows;
        builder.roundingMode = roundingMode;
        return builder;
    }

    public LocalDate startDateIncl() {
        return this.startDateIncl;
    }

    public LocalDate endDateIncl() {
        return this.endDateIncl;
    }

    public BigDecimal startAssetValueExcl() {
        return this.startAssetValueExcl;
    }

    public BigDecimal endAssetValueIncl() {
        return this.endAssetValueIncl;
    }

    public Function<LocalDate, BigDecimal> assetValues() {
        return this.assetValues;
    }

    public SortedMap<LocalDate, BigDecimal> flows() {
        return this.flows;
    }

    public int largeFlowLevelInPercent() {
        return this.largeFlowLevelInPercent;
    }

    public FlowTiming flowTiming() {
        return this.flowTiming;
    }

    public AnnualizationOption annualization() {
        return this.annualization;
    }

    public boolean resultInPercent() {
        return this.resultInPercent;
    }

    public int calcScale() {
        return this.calcScale;
    }

    public int resultScale() {
        return this.resultScale;
    }

    public RoundingMode roundingMode() {
        return this.roundingMode;
    }

    public static class PerfCalcRequestBuilder {

        private LocalDate startDateIncl;
        private LocalDate endDateIncl;
        private BigDecimal startAssetValueExcl;
        private BigDecimal endAssetValueIncl;
        private Function<LocalDate, BigDecimal> assetValuesProvider;
        private Map<LocalDate, BigDecimal> assetValuesMap;
        private Collection<DateAmount> assetValuesCollection;
        private Map<LocalDate, BigDecimal> flowsMap;
        private Collection<DateAmount> flowsCollection;
        private Integer largeFlowLevelInPercent;
        private FlowTiming flowTiming;
        private AnnualizationOption annualization;
        private Boolean resultInPercent;
        private Integer calcScale;
        private Integer resultScale;
        private RoundingMode roundingMode;

        private PerfCalcRequestBuilder() {
        }

        public PerfCalcRequestBuilder assetValues(Function<LocalDate, BigDecimal> assetValues) {
            this.assetValuesProvider = assetValues;
            this.assetValuesMap = null;
            this.assetValuesCollection = null;
            return this;
        }

        public PerfCalcRequestBuilder assetValues(Map<LocalDate, BigDecimal> assetValues) {
            this.assetValuesProvider = null;
            this.assetValuesMap = assetValues;
            this.assetValuesCollection = null;
            return this;
        }

        public PerfCalcRequestBuilder assetValues(Collection<DateAmount> assetValues) {
            this.assetValuesProvider = null;
            this.assetValuesMap = null;
            this.assetValuesCollection = assetValues;
            return this;
        }

        public PerfCalcRequestBuilder flows(Map<LocalDate, BigDecimal> flows) {
            this.flowsCollection = null;
            this.flowsMap = flows;
            return this;
        }

        public PerfCalcRequestBuilder flows(Collection<DateAmount> flows) {
            this.flowsMap = null;
            this.flowsCollection = flows;
            return this;
        }

        public PerfCalcRequest build() {
            return new PerfCalcRequest(
                    startDateIncl,
                    endDateIncl,
                    startAssetValueExcl,
                    endAssetValueIncl,
                    assetValuesProvider,
                    assetValuesMap,
                    assetValuesCollection,
                    flowsMap,
                    flowsCollection,
                    largeFlowLevelInPercent,
                    flowTiming,
                    annualization,
                    resultInPercent,
                    calcScale,
                    resultScale,
                    roundingMode);
        }

        public PerfCalcRequestBuilder copy() {
            PerfCalcRequestBuilder copy = new PerfCalcRequestBuilder();
            copy.startDateIncl = startDateIncl;
            copy.endDateIncl = endDateIncl;
            copy.startAssetValueExcl = startAssetValueExcl;
            copy.endAssetValueIncl = endAssetValueIncl;
            copy.largeFlowLevelInPercent = largeFlowLevelInPercent;
            copy.flowTiming = flowTiming;
            copy.annualization = annualization;
            copy.resultInPercent = resultInPercent;
            copy.calcScale = calcScale;
            copy.resultScale = resultScale;
            copy.assetValuesProvider = assetValuesProvider;
            copy.assetValuesMap = assetValuesMap;
            copy.assetValuesCollection = assetValuesCollection;
            copy.flowsMap = flowsMap;
            copy.flowsCollection = flowsCollection;
            copy.roundingMode = roundingMode;
            return copy;
        }

        public PerfCalcRequestBuilder startDateIncl(LocalDate startDateIncl) {
            this.startDateIncl = startDateIncl;
            return this;
        }

        public PerfCalcRequestBuilder endDateIncl(LocalDate endDateIncl) {
            this.endDateIncl = endDateIncl;
            return this;
        }

        public PerfCalcRequestBuilder startAssetValueExcl(BigDecimal startAssetValueExcl) {
            this.startAssetValueExcl = startAssetValueExcl;
            return this;
        }

        public PerfCalcRequestBuilder endAssetValueIncl(BigDecimal endAssetValueIncl) {
            this.endAssetValueIncl = endAssetValueIncl;
            return this;
        }

        public PerfCalcRequestBuilder largeFlowLevelInPercent(Integer largeFlowLevelInPercent) {
            this.largeFlowLevelInPercent = largeFlowLevelInPercent;
            return this;
        }

        public PerfCalcRequestBuilder flowTiming(FlowTiming flowTiming) {
            this.flowTiming = flowTiming;
            return this;
        }

        public PerfCalcRequestBuilder annualization(AnnualizationOption annualization) {
            this.annualization = annualization;
            return this;
        }

        public PerfCalcRequestBuilder resultInPercent(Boolean resultInPercent) {
            this.resultInPercent = resultInPercent;
            return this;
        }

        public PerfCalcRequestBuilder calcScale(Integer calcScale) {
            this.calcScale = calcScale;
            return this;
        }

        public PerfCalcRequestBuilder resultScale(Integer resultScale) {
            this.resultScale = resultScale;
            return this;
        }

        public PerfCalcRequestBuilder roundingMode(RoundingMode roundingMode) {
            this.roundingMode = roundingMode;
            return this;
        }
    }

}
