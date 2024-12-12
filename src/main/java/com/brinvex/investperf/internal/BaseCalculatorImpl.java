package com.brinvex.investperf.internal;


import com.brinvex.investperf.api.Annualizer;
import com.brinvex.investperf.api.FlowTiming;
import com.brinvex.investperf.api.PerfCalcRequest;
import com.brinvex.investperf.api.PerformanceCalculator;
import com.brinvex.java.Num;
import com.brinvex.java.collection.CollectionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map.Entry;
import java.util.SortedMap;

import static java.math.BigDecimal.ZERO;

abstract class BaseCalculatorImpl implements PerformanceCalculator {

    @Override
    public final BigDecimal calculateReturn(PerfCalcRequest perfCalcRequest) {

        SortedMap<LocalDate, BigDecimal> flows = perfCalcRequest.flows();
        BigDecimal startValueExcl = perfCalcRequest.startAssetValueExcl();
        BigDecimal endValueIncl = perfCalcRequest.endAssetValueIncl();
        LocalDate startDateIncl = perfCalcRequest.startDateIncl();
        LocalDate endDateIncl = perfCalcRequest.endDateIncl();
        FlowTiming flowTiming = perfCalcRequest.flowTiming();
        if (!flows.isEmpty()) {
            switch (flowTiming) {
                case BEGINNING_OF_DAY -> {
                    Entry<LocalDate, BigDecimal> firstFlowEntry = flows.firstEntry();
                    LocalDate firstFlowDate = firstFlowEntry.getKey();
                    if (firstFlowDate.isEqual(startDateIncl)) {
                        startValueExcl = startValueExcl.add(firstFlowEntry.getValue());
                        flows = CollectionUtil.rangeSafeTailMap(flows, firstFlowDate.plusDays(1));
                    }
                }
                case END_OF_DAY -> {
                    Entry<LocalDate, BigDecimal> lastFlowEntry = flows.lastEntry();
                    LocalDate lastFlowDate = lastFlowEntry.getKey();
                    if (lastFlowDate.isEqual(endDateIncl)) {
                        endValueIncl = endValueIncl.subtract(lastFlowEntry.getValue());
                        flows = CollectionUtil.rangeSafeHeadMap(flows, lastFlowDate);
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + flowTiming);
            }
        }

        if (startValueExcl.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException((
                    "startValueExcl must be greater than zero; " +
                    "given: startValueExcl=%s, endValueIncl=%s, startDateIncl=%s, endDateIncl=%s, %s, flows=%s")
                    .formatted(startValueExcl, endValueIncl, startDateIncl, endDateIncl, flowTiming, flows));
        }

        BigDecimal cumulReturn;
        if (flows.isEmpty()) {
            cumulReturn = SimpleReturnCalculatorImpl.calculateSimpleCumulReturn(
                    startValueExcl,
                    endValueIncl,
                    perfCalcRequest.calcScale(),
                    perfCalcRequest.roundingMode()
            );
        } else {
            PerfCalcRequest adjPerfCalcRequest = perfCalcRequest.toBuilder()
                    .startAssetValueExcl(startValueExcl)
                    .endAssetValueIncl(endValueIncl)
                    .flows(flows)
                    .build();
            cumulReturn = calculateCumulativeReturn(adjPerfCalcRequest);
        }

        BigDecimal unscaledAnnReturn = Annualizer.INSTANCE.annualizeReturn(
                perfCalcRequest.annualization(),
                cumulReturn,
                startDateIncl,
                endDateIncl
        );
        if (perfCalcRequest.resultInPercent()) {
            unscaledAnnReturn = unscaledAnnReturn.multiply(Num._100);
        }
        return unscaledAnnReturn.setScale(perfCalcRequest.resultScale(), perfCalcRequest.roundingMode());
    }

    protected abstract BigDecimal calculateCumulativeReturn(PerfCalcRequest calcRequest);
}
