package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/** ADD, SUBTRACT and MULTIPLY are exact: no MathContext, no rounding (BR-3). */
public enum Operation {
    ADD {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }
    },
    SUBTRACT {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }
    },
    MULTIPLY {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.multiply(b);
        }
    };

    public abstract BigDecimal apply(BigDecimal a, BigDecimal b);

    public static Operation named(String name) {
        return Arrays.stream(values())
                .filter(operation -> operation.name().equals(name))
                .findFirst()
                .orElseThrow(UnknownOperationException::new);
    }

    public static String supportedNames() {
        return Arrays.stream(values()).map(Operation::name).collect(Collectors.joining(", "));
    }
}
