package com.brinvex.investperf.internal.util;

import java.util.function.Function;

public class NullUtil {

    public static <I, O> O nullSafe(I input, Function<I, O> mapFnc) {
        return input == null ? null : mapFnc.apply(input);
    }
}
