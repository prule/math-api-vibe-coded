package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

/** ADD, SUBTRACT and MULTIPLY are exact; DIVIDE rounds to DECIMAL128 (BR-3). */
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
    },
    DIVIDE {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) {
                throw new DivisionByZeroException(); // BR-2: every zero form has signum 0
            }
            return a.divide(b, MathContext.DECIMAL128);
        }
    };

    public abstract BigDecimal apply(BigDecimal a, BigDecimal b);

    public static Operation named(String name) {
        return Arrays.stream(values())
                .filter(operation -> operation.name().equals(name))
                .findFirst()
                .orElseThrow(UnknownOperationException::new);
    }

    public static List<String> names() {
        return Arrays.stream(values()).map(Operation::name).toList();
    }

    public static String supportedNames() {
        return String.join(", ", names());
    }
}
