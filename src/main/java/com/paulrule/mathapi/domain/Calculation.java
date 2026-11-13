package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    public Calculation {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
    }

    /** The Result, with trailing zeros stripped (BR-4). Plain-notation rendering is the adapter's job. */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }
}
