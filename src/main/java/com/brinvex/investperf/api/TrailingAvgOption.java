package com.brinvex.investperf.api;

/**
 * How a trailing 1-year average - of profit, flow or income - is taken while fewer than a year of
 * periods has been calculated, that is over the first year after the calculation starts.
 */
public enum TrailingAvgOption {

    /**
     * The average of the periods there are. True from the first period on, but resting on one or two
     * periods at first, so a single large one sets it.
     */
    AVERAGE_AVAILABLE_PERIODS,

    /**
     * The sum of the periods there are divided by a full year of periods, as if the missing ones were
     * zero. Never extreme, but understated until a year has passed.
     */
    DIVIDE_BY_FULL_YEAR,

    /**
     * No average until a full year of periods has been calculated, as no trailing 1-year return is
     * given before then either.
     */
    REQUIRE_FULL_YEAR
}
