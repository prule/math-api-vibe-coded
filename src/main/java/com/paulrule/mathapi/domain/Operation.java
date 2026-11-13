package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

/** ADD, SUBTRACT, MULTIPLY, MODULO and NEGATE are exact; DIVIDE, POWER and SQRT round to DECIMAL128 (BR-3). */
public enum Operation {
    ADD(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }
    },
    SUBTRACT(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }
    },
    MULTIPLY(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.multiply(b);
        }
    },
    DIVIDE(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) {
                throw new DivisionByZeroException(); // BR-2: every zero form has signum 0
            }
            return a.divide(b, MathContext.DECIMAL128);
        }
    },
    POWER(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            // BR-5, checked before any arithmetic: the cheap bound first, so 1E+1000 is never stripped or expanded.
            if (b.abs().compareTo(MAX_EXPONENT) > 0 || b.stripTrailingZeros().scale() > 0) {
                throw new InvalidExponentException();
            }
            int exponent = b.intValueExact();
            if (a.signum() == 0 && exponent < 0) {
                throw new DivisionByZeroException(); // BR-2
            }
            // BR-3: the exact power, rounded once. BigDecimal.pow(int, MathContext) may be off in the last digit.
            BigDecimal exact = a.pow(Math.abs(exponent));
            BigDecimal result = exponent < 0
                    ? BigDecimal.ONE.divide(exact, MathContext.DECIMAL128)
                    : exact.round(MathContext.DECIMAL128);
            if (Math.abs(result.stripTrailingZeros().scale()) > Calculation.MAX_SCALE) {
                throw new ResultOutOfRangeException(); // BR-6
            }
            return result;
        }
    },
    MODULO(2) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) {
                throw new DivisionByZeroException(); // BR-2
            }
            return a.remainder(b); // BR-7: truncated toward zero, so it takes the sign of a
        }
    },
    SQRT(1) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (a.signum() < 0) {
                throw new NegativeSquareRootException(); // BR-8: every zero form has signum 0, so its root is 0
            }
            return a.sqrt(MathContext.DECIMAL128); // correctly rounded under HALF_EVEN (BR-3)
        }
    },
    NEGATE(1) {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.negate();
        }
    };

    private final int arity;

    Operation(int arity) {
        this.arity = arity;
    }

    /** How many Operands this Operation takes: 1 (a only) or 2 (a and b) (BR-9). */
    public int arity() {
        return arity;
    }

    /** BR-5: the largest |exponent| POWER accepts. */
    static final BigDecimal MAX_EXPONENT = BigDecimal.valueOf(999);

    /** {@code b} is null for a unary Operation. */
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
