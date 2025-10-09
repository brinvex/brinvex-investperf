## Brinvex Investment Performance Analyzer

The _Brinvex Investment Performance Analyzer_ (technically named _brinvex-investperf_) 
is a compact Java library designed for calculating and analyzing financial investment performance,
guided by the principles of the _Global Investment Performance Standards (GIPS)_ 
to ensure accuracy and consistency.

The following examples demonstrate how easily and fluently one can use the tool to calculate 
and analyze financial investment performance. 
With minimal setup, you can define key parameters such as asset values and cash flows, 
and quickly get back a detailed performance report, including TWR, MWR any many more. 

````java
List<PerfAnalysis> perfAnalyses = PerformanceAnalyzer.INSTANCE.analyzePerformance(PerfAnalysisRequest.builder()
    .resultStartDateIncl(parse("2023-01-01"))
    .resultEndDateIncl(parse("2023-03-31"))
    .assetValues(List.of(
            new DateAmount("2022-12-31", "100000"),
            new DateAmount("2023-01-31", "98000"),
            new DateAmount("2023-02-28", "117000"),
            new DateAmount("2023-03-31", "120000")
    ))
    .flows(List.of(
            new DateAmount("2023-01-20", "2000"),
            new DateAmount("2023-02-15", "1000"),
            new DateAmount("2023-02-07", "-1500")
    ))
    .flowTiming(BEGINNING_OF_DAY)
    .twrCalculatorType(LinkedModifiedDietzTwrCalculator.class)
    .mwrCalculatorType(ModifiedDietzMwrCalculator.class)
    .resultFrequency(MONTH)
    .resultRatesInPercent(true)
    .resultScale(2)
    .calculateMwr(true)
    .calculatePeriodMwr(true)
    .calculateTrailingAvgProfit1Y(true)
    .calculateTrailingAvgFlow1Y(true)
    .build());
````

|                        | 2023-01 | 2023-02 | 2023-03 |
|------------------------|--------:|--------:|--------:|
| Period Start Value     |  100000 |   98000 |  117000 |
| Period End Value       |   98000 |  117000 |  120000 |
| Period Flow            |    2000 |    -500 |       0 |
| Period TWR             |  -3.97% |  20.04% |   2.56% |
| Cumulative TWR         |  -3.97% |  15.27% |  18.23% |
| **Annualized TWR**     |  -3.97% |  15.27% |  18.23% |
| Period MWR             |  -3.97% |  20.04% |   2.56% |
| Cumulative MWR         |  -3.97% |  15.34% |  18.28% |
| **Annualized MWR**     |  -3.97% |  15.34% |  18.28% |
| Total Contribution     |  102000 |  101500 |  101500 |
| Period Profit          |   -4000 |   19500 |    3000 |
| Total Profit           |   -4000 |   15500 |   18500 |
| Trailing Avg Profit 1Y | -333.00 | 1292.00 | 1542.67 |
| Trailing Avg Flow 1Y   |  167.00 |  125.00 |  125.00 |

### True Time-Weighted Rate of Return Calculator
_True Time-Weighted Return (TWR)_  is often the preferred method for evaluating portfolio performance
as it eliminates the impact of cash flows.
This metric focuses solely on the portfolio's ability to generate returns,
making it ideal for performance comparison across different funds or investment strategies.
````java
TwrCalculator twrCalculator = PerformanceCalculator.trueTwrCalculator();
BigDecimal twrReturn = twrCalculator.calculateReturn(PerfCalcRequest.builder()
        .startDateIncl(parse("2020-06-01"))
        .endDateIncl(parse("2020-06-30"))
        .startAssetValueExcl(new BigDecimal("100000"))
        .endAssetValueIncl(new BigDecimal("135000"))
        .flows(List.of(
                new DateAmount("2020-06-06", "-2000"),
                new DateAmount("2020-06-11", "20000")))
        .assetValues(List.of(
                new DateAmount("2020-06-05", "101000"),
                new DateAmount("2020-06-10", "132000")))
        .flowTiming(BEGINNING_OF_DAY)
        .annualization(DO_NOT_ANNUALIZE)
        .resultInPercent(true)
        .resultScale(4)
        .build());
assertEquals("19.6053", twrReturn.toPlainString());
````
#### Time-Weighted Rate of Return - Distorting effects of contributions
See the demo spreadsheet:  ```Demo_TWR_Distorting_effects_of_contributions.xlsx```

### Modified Dietz Money-Weighted Rate of Return Calculator

_Modified Dietz_ is a simplified version of the _Money-Weighted Return (MWR)_ calculation. 
It adjusts for the timing and size of cash flows to provide a performance 
measure that accounts for external influences like deposits and withdrawals.
It is one of the methodologies of calculating returns recommended 
by the _Investment Performance Council (IPC)_ as part of their _Global Investment Performance Standards (GIPS)_.

````java
ModifiedDietzMwrCalculator mwrCalculator = PerformanceCalculator.modifiedDietzMwrCalculator();                                                      
BigDecimal mwrReturn = mwrCalculator.calculateReturn(PerfCalcRequest.builder()              
        .startDateIncl(parse("2020-06-01"))                                                 
        .endDateIncl(parse("2020-06-30"))                                                   
        .startAssetValueExcl(new BigDecimal("100000"))                                      
        .endAssetValueIncl(new BigDecimal("135000"))                                        
        .flows(List.of(                                                                     
                new DateAmount("2020-06-06", "-2000"),                                      
                new DateAmount("2020-06-11", "20000")))                                     
        .flowTiming(BEGINNING_OF_DAY)                                                       
        .annualization(DO_NOT_ANNUALIZE)                                                    
        .resultInPercent(true)                                                              
        .resultScale(4)                                                                     
        .build());                                                                          
assertEquals("15.2239", mwrReturn.toPlainString());                                         
````

If you're considering adding support for the _XIRR MWR_ calculation method, 
check out this library https://github.com/RayDeCampo/java-xirr.  
I used XIRR previously, but with a focus on computational efficiency, 
I switched to the Modified Dietz method in 2024, 
as it is significantly more efficient and meets our needs well. 
Notably, both _XIRR_ and _Modified Dietz_ methods are endorsed by the _GIPS_.

### Linked Modified Dietz Time-Weighted Rate of Return Calculator
The _Linked Modified Dietz Time-Weighted Rate of Return_ calculation method allows 
you to approximate the time-weighted rate of return, even when portfolio valuations at cash flow dates are unavailable.
````
LinkedModifiedDietzTwrCalculator linkedTwrCalculator = PerformanceCalculator.linkedModifiedDietzTwrCalculator();
BigDecimal twrReturn = linkedTwrCalculator.calculateReturn(PerfCalcRequest.builder()
        .startDateIncl(parse("2021-01-01"))
        .endDateIncl(parse("2021-03-31"))
        .startAssetValueExcl(new BigDecimal("10000"))
        .endAssetValueIncl(new BigDecimal("10200"))
        .flows(List.of(
                new DateAmount("2021-02-15", "100")))
        .assetValues(List.of(
                new DateAmount("2021-01-31", "10100"),
                new DateAmount("2021-02-28", "10201")
        ))
        .flowTiming(BEGINNING_OF_DAY)
        .resultScale(10)
        .annualization(DO_NOT_ANNUALIZE)
        .build());
assertEquals("0.0100004877", twrReturn.toPlainString());
````

#### Resources for learning more about Investment Performance Calculation

https://canadianportfoliomanagerblog.com/calculating-your-modified-dietz-rate-of-return  
https://www.interactivebrokers.com/images/common/Statements/MWR-TWR_white_paper.pdf  
https://en.wikipedia.org/wiki/Modified_Dietz_method  
https://en.wikipedia.org/wiki/Time-weighted_return  
https://www.gipsstandards.org  

## Maven and JPMS Setup
````
<properties>
     <brinvex-investperf.version>1.0.7</brinvex-investperf.version>
</properties>

<repository>
    <id>github-pubrepo-brinvex</id>
    <name>Github Public Repository - Brinvex</name>
    <url>https://github.com/brinvex/brinvex-pubrepo/raw/main/</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex</groupId>
    <artifactId>brinvex-investperf</artifactId>
    <version>${brinvex-investperf.version}</version>
</dependency>
````

The library supports JPMS and exports the module named ````com.brinvex.investperf````.

## Requirements

- Java 23 or above

## License

- The _Brinvex Investment Performance Analyzer_ is released under version 2.0 of the Apache License.
