package com.brinvex.investperf.internal;

import com.brinvex.investperf.api.FlowTiming;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceCalculator;
import com.brinvex.java.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map.Entry;
import java.util.SortedMap;

import static java.lang.Math.toIntExact;
import static java.math.BigDecimal.ZERO;
import static java.time.temporal.ChronoUnit.DAYS;

public class ModifiedDietzMwrCalculatorImpl extends BaseCalculatorImpl implements PerformanceCalculator.ModifiedDietzMwrCalculator {

    public static final ModifiedDietzMwrCalculator INSTANCE = new ModifiedDietzMwrCalculatorImpl();

    private ModifiedDietzMwrCalculatorImpl() {
    }

    @Override
    protected BigDecimal calculateCumulativeReturn(PerfCalcRequest calcReq) {
        LocalDate startDateIncl = calcReq.startDateIncl();
        LocalDate endDateIncl = calcReq.endDateIncl();
        LocalDate endDateExcl = endDateIncl.plusDays(1);
        BigDecimal startValueExcl = calcReq.startAssetValueExcl();
        BigDecimal endValueIncl = calcReq.endAssetValueIncl();
        SortedMap<LocalDate, BigDecimal> flows = calcReq.flows();
        FlowTiming flowTiming = calcReq.flowTiming();
        int calcScale = calcReq.calcScale();
        RoundingMode roundingMode = calcReq.roundingMode();
        int totalDays = toIntExact(DAYS.between(startDateIncl, endDateExcl));
        BigDecimal totalDaysDecimal = new BigDecimal(totalDays);

        BigDecimal flowSum = ZERO;
        BigDecimal weightedFlowSum = ZERO;
        int flowTimingWeightAdjuster = switch (flowTiming) {
            case BEGINNING_OF_DAY -> 0;
            case END_OF_DAY -> -1;
        };
        for (Entry<LocalDate, BigDecimal> flow : flows.entrySet()) {
            LocalDate flowDate = flow.getKey();
            BigDecimal flowValue = flow.getValue();

            int weightNumerator = toIntExact(DAYS.between(flowDate, endDateExcl)) + flowTimingWeightAdjuster;
            if (weightNumerator >= totalDays || weightNumerator <= 0) {
                throw new IllegalArgumentException((
                        "flowDate out of range; " +
                        "given: flowDate=%s, startDateIncl=%s, endDateIncl=%s, flowTiming=%s, weightNumerator=%s, totalDays=%s")
                        .formatted(flowDate, startDateIncl, endDateIncl, flowTiming, weightNumerator, totalDays));
            }

            BigDecimal weight = new BigDecimal(weightNumerator).divide(totalDaysDecimal, calcScale, roundingMode);
            BigDecimal weightedFlowValue = flowValue.multiply(weight);

            flowSum = flowSum.add(flowValue);
            weightedFlowSum = weightedFlowSum.add(weightedFlowValue);
        }
        if (startValueExcl.compareTo(weightedFlowSum.negate()) <= 0) {
            //See https://en.wikipedia.org/wiki/Modified_Dietz_method#Negative_or_zero_average_capital
            throw new IllegalStateException((
                    "Could not calculate ModifiedDietz return of given data: " +
                    "adjStartValueExcl=%s, adjEndValueIncl=%s, " +
                    "weightedFlowSum=%s, periodFlow=%s, " +
                    "startDateIncl=%s, endDateIncl=%s")
                    .formatted(
                            startValueExcl, endValueIncl,
                            weightedFlowSum, flows,
                            startDateIncl, endDateIncl
                    ));
        }

        BigDecimal gain = endValueIncl.subtract(startValueExcl).subtract(flowSum);
        BigDecimal averageCapital = startValueExcl.add(weightedFlowSum);

        BigDecimal cumulReturn = gain.divide(averageCapital, calcScale, roundingMode);
        if (cumulReturn.compareTo(Num.MINUS_1) < 0) {
            cumulReturn = Num.MINUS_1;
        }
        return cumulReturn;
    }

}
