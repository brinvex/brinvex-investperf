package com.brinvex.ipa.api;


import com.brinvex.ipa.internal.PerformanceAnalyzerImpl;

import java.util.SequencedCollection;

public interface PerformanceAnalyzer {

    PerformanceAnalyzer INSTANCE = new PerformanceAnalyzerImpl();

    SequencedCollection<PerfAnalysis> analyzePerformance(PerfAnalysisRequest perfAnalysisRequest);

}
