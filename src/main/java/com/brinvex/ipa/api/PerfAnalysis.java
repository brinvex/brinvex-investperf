package com.brinvex.ipa.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the performance details for a specific period.
 *
 * @param periodStartDateIncl       The start date of the period, inclusive
 * @param periodEndDateIncl         The end date of the period, inclusive
 * @param periodCaption             The period caption
 * @param periodStartAssetValueExcl The value of the asset at the start of the period, exclusive of any flows
 * @param periodEndAssetValueIncl   The value of the asset at the end of the period, inclusive of any flows
 * @param periodFlow                The total external cash flow during the period (e.g., deposits or withdrawals)
 * @param periodTwr                 Non-Annualized Time-Weighted Return for this sub-period
 * @param cumulativeTwr             Cumulative Time-Weighted Return up to and including this sub-period
 * @param annualizedTwr             Annualized Time-Weighted Return up to and including this sub-period
 * @param cumulativeMwr             Cumulative Money-Weighted Return up to and including this sub-period
 * @param annualizedMwr             Annualized Money-Weighted Return up to and including this sub-period
 * @param totalContribution         The sum of the initial asset value and all subsequent cash flows up to and including this sub-period.
 *                                  This value does not account for any gains or losses that occurred due to investment performance.
 * @param periodProfit              The profit during the period, calculated as the difference between the ending asset value and the start value,
 *                                  adjusted for any flows within the period.
 * @param totalProfit               The total cumulative profit up to and including this period.
 * @param periodIncome              The income generated during the period, such as dividends or interest.
 * @param trailingAvgProfit1Y       The average profit over the trailing 12-month period.
 * @param trailingAvgFlow1Y         The average cash flow over the trailing 12-month period.
 * @param trailingAvgIncome1Y       The average income over the trailing 12-month period.
 * @param trailingTwr1Y             The trailing Time-Weighted Return over the past 1 year.
 * @param trailingTwr2Y             The trailing Time-Weighted Return over the past 2 years.
 * @param trailingTwr3Y             The trailing Time-Weighted Return over the past 3 years.
 * @param trailingTwr5Y             The trailing Time-Weighted Return over the past 5 years.
 * @param trailingTwr10Y            The trailing Time-Weighted Return over the past 10 years.
 */
public record PerfAnalysis(
        LocalDate periodStartDateIncl,
        LocalDate periodEndDateIncl,
        String periodCaption,
        BigDecimal periodStartAssetValueExcl,
        BigDecimal periodEndAssetValueIncl,
        BigDecimal periodFlow,
        BigDecimal periodTwr,
        BigDecimal cumulativeTwr,
        BigDecimal annualizedTwr,
        BigDecimal cumulativeMwr,
        BigDecimal annualizedMwr,
        BigDecimal totalContribution,
        BigDecimal periodProfit,
        BigDecimal totalProfit,
        BigDecimal periodIncome,
        BigDecimal trailingAvgProfit1Y,
        BigDecimal trailingAvgFlow1Y,
        BigDecimal trailingAvgIncome1Y,
        BigDecimal trailingTwr1Y,
        BigDecimal trailingTwr2Y,
        BigDecimal trailingTwr3Y,
        BigDecimal trailingTwr5Y,
        BigDecimal trailingTwr10Y
) {
    public static PerfAnalysisBuilder builder() {
        return new PerfAnalysisBuilder();
    }

    public static class PerfAnalysisBuilder {
        private LocalDate periodStartDateIncl;
        private LocalDate periodEndDateIncl;
        private String periodCaption;
        private BigDecimal periodStartAssetValueExcl;
        private BigDecimal periodEndAssetValueIncl;
        private BigDecimal periodFlow;
        private BigDecimal periodTwr;
        private BigDecimal cumulativeTwr;
        private BigDecimal annualizedTwr;
        private BigDecimal cumulativeMwr;
        private BigDecimal annualizedMwr;
        private BigDecimal totalContribution;
        private BigDecimal periodProfit;
        private BigDecimal totalProfit;
        private BigDecimal periodIncome;
        private BigDecimal trailingAvgProfit1Y;
        private BigDecimal trailingAvgFlow1Y;
        private BigDecimal trailingAvgIncome1Y;
        private BigDecimal trailingTwr1Y;
        private BigDecimal trailingTwr2Y;
        private BigDecimal trailingTwr3Y;
        private BigDecimal trailingTwr5Y;
        private BigDecimal trailingTwr10Y;

        PerfAnalysisBuilder() {
        }

        public PerfAnalysisBuilder periodStartDateIncl(LocalDate periodStartDateIncl) {
            this.periodStartDateIncl = periodStartDateIncl;
            return this;
        }

        public PerfAnalysisBuilder periodEndDateIncl(LocalDate periodEndDateIncl) {
            this.periodEndDateIncl = periodEndDateIncl;
            return this;
        }

        public PerfAnalysisBuilder periodCaption(String periodCaption) {
            this.periodCaption = periodCaption;
            return this;
        }

        public PerfAnalysisBuilder periodStartAssetValueExcl(BigDecimal periodStartAssetValueExcl) {
            this.periodStartAssetValueExcl = periodStartAssetValueExcl;
            return this;
        }

        public PerfAnalysisBuilder periodEndAssetValueIncl(BigDecimal periodEndAssetValueIncl) {
            this.periodEndAssetValueIncl = periodEndAssetValueIncl;
            return this;
        }

        public PerfAnalysisBuilder periodFlow(BigDecimal periodFlow) {
            this.periodFlow = periodFlow;
            return this;
        }

        public PerfAnalysisBuilder periodTwr(BigDecimal periodTwr) {
            this.periodTwr = periodTwr;
            return this;
        }

        public PerfAnalysisBuilder cumulativeTwr(BigDecimal cumulativeTwr) {
            this.cumulativeTwr = cumulativeTwr;
            return this;
        }

        public PerfAnalysisBuilder annualizedTwr(BigDecimal annualizedTwr) {
            this.annualizedTwr = annualizedTwr;
            return this;
        }

        public PerfAnalysisBuilder cumulativeMwr(BigDecimal cumulativeMwr) {
            this.cumulativeMwr = cumulativeMwr;
            return this;
        }

        public PerfAnalysisBuilder annualizedMwr(BigDecimal annualizedMwr) {
            this.annualizedMwr = annualizedMwr;
            return this;
        }

        public PerfAnalysisBuilder totalContribution(BigDecimal totalContribution) {
            this.totalContribution = totalContribution;
            return this;
        }

        public PerfAnalysisBuilder periodProfit(BigDecimal periodProfit) {
            this.periodProfit = periodProfit;
            return this;
        }

        public PerfAnalysisBuilder totalProfit(BigDecimal totalProfit) {
            this.totalProfit = totalProfit;
            return this;
        }

        public PerfAnalysisBuilder periodIncome(BigDecimal periodIncome) {
            this.periodIncome = periodIncome;
            return this;
        }

        public PerfAnalysisBuilder trailingAvgProfit1Y(BigDecimal trailingAvgProfit1Y) {
            this.trailingAvgProfit1Y = trailingAvgProfit1Y;
            return this;
        }

        public PerfAnalysisBuilder trailingAvgFlow1Y(BigDecimal trailingAvgFlow1Y) {
            this.trailingAvgFlow1Y = trailingAvgFlow1Y;
            return this;
        }

        public PerfAnalysisBuilder trailingAvgIncome1Y(BigDecimal trailingAvgIncome1Y) {
            this.trailingAvgIncome1Y = trailingAvgIncome1Y;
            return this;
        }

        public PerfAnalysisBuilder trailingTwr1Y(BigDecimal trailingTwr1Y) {
            this.trailingTwr1Y = trailingTwr1Y;
            return this;
        }

        public PerfAnalysisBuilder trailingTwr2Y(BigDecimal trailingTwr2Y) {
            this.trailingTwr2Y = trailingTwr2Y;
            return this;
        }

        public PerfAnalysisBuilder trailingTwr3Y(BigDecimal trailingTwr3Y) {
            this.trailingTwr3Y = trailingTwr3Y;
            return this;
        }

        public PerfAnalysisBuilder trailingTwr5Y(BigDecimal trailingTwr5Y) {
            this.trailingTwr5Y = trailingTwr5Y;
            return this;
        }

        public PerfAnalysisBuilder trailingTwr10Y(BigDecimal trailingTwr10Y) {
            this.trailingTwr10Y = trailingTwr10Y;
            return this;
        }

        public PerfAnalysis build() {
            return new PerfAnalysis(
                    this.periodStartDateIncl,
                    this.periodEndDateIncl,
                    this.periodCaption,
                    this.periodStartAssetValueExcl,
                    this.periodEndAssetValueIncl,
                    this.periodFlow,
                    this.periodTwr,
                    this.cumulativeTwr,
                    this.annualizedTwr,
                    this.cumulativeMwr,
                    this.annualizedMwr,
                    this.totalContribution,
                    this.periodProfit,
                    this.totalProfit,
                    this.periodIncome,
                    this.trailingAvgProfit1Y,
                    this.trailingAvgFlow1Y,
                    this.trailingAvgIncome1Y,
                    this.trailingTwr1Y,
                    this.trailingTwr2Y,
                    this.trailingTwr3Y,
                    this.trailingTwr5Y,
                    this.trailingTwr10Y);
        }
    }
}
