package com.brinvex.investperf.api;


import com.brinvex.investperf.internal.PerformanceAnalyzerImpl;

import java.util.SequencedCollection;

public interface PerformanceAnalyzer {

    PerformanceAnalyzer INSTANCE = new PerformanceAnalyzerImpl();

    SequencedCollection<PerfAnalysis> analyzePerformance(PerfAnalysisRequest perfAnalysisRequest);

}
