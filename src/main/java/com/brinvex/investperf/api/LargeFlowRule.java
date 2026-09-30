package com.brinvex.investperf.api;

/**
 * Which cash flows count as large. A linked Modified Dietz TWR revalues the portfolio at every
 * large flow, as GIPS asks, and otherwise measures each month as a single Modified Dietz period.
 */
public sealed interface LargeFlowRule {

    /**
     * A flow above 5% of the value it flows into is large.
     */
    LargeFlowRule DEFAULT = new AbovePercent(5);

    /**
     * No flow is large, so every month is a single Modified Dietz period whatever flows it holds.
     * Suited to an account whose only gain is interest credited at month end, such as a savings
     * account: revaluing at a withdrawal would measure the whole month's interest against whatever
     * the withdrawal left, while the month's Modified Dietz return measures it against the balance
     * averaged over the days it was held.
     */
    record Never() implements LargeFlowRule {
    }

    /**
     * A flow above this percentage of the value it flows into is large.
     */
    record AbovePercent(int percent) implements LargeFlowRule {
        public AbovePercent {
            if (percent <= 0) {
                throw new IllegalArgumentException("percent must be greater than zero; given: %s".formatted(percent));
            }
        }
    }
}
