package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** {@code b} is null exactly when the Operation is unary (BR-9). */
public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    public Calculation {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(a, "a");
        if ((b == null) != (operation.arity() == 1)) {
            throw new ArityMismatchException(operation); // BR-9, before BR-1
        }
        requireInRange(a, "a");
        if (b != null) {
            requireInRange(b, "b");
        }
    }

    static final int MAX_PRECISION = 100;
    static final int MAX_SCALE = 1000;

    /** BR-1, on the Operand as written: checked here so no arithmetic ever runs on an oversized Operand. */
    private static void requireInRange(BigDecimal operand, String name) {
        if (operand.precision() > MAX_PRECISION || Math.abs(operand.scale()) > MAX_SCALE) {
            throw new OperandOutOfRangeException(name);
        }
    }

    /** The Result, with trailing zeros stripped (BR-4). Plain-notation rendering is the adapter's job. */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }
}
