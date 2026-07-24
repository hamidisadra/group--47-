package com.pvz.model.support;

import java.util.function.Supplier;


@Deprecated
public final class ConditionalValue {
    private ConditionalValue() {
    }
    public static <T> T select(boolean condition,
                               Supplier<T> whenTrue,
                               Supplier<T> whenFalse) {
        if (whenTrue == null || whenFalse == null) {
            throw new IllegalArgumentException("Conditional branches cannot be null.");
        }
        if (condition) {
            return whenTrue.get();
        }
        return whenFalse.get();
    }
}
