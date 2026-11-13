package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculationTest {

    private static String result(Operation operation, String a, String b) {
        return new Calculation(operation, new BigDecimal(a), new BigDecimal(b))
                .result()
                .toPlainString();
    }

    @Test
    void addsPointOneAndPointTwoExactly() {
        assertThat(result(Operation.ADD, "0.1", "0.2")).isEqualTo("0.3");
    }

    @Test
    void addsNegatives() {
        assertThat(result(Operation.ADD, "-1.5", "-2.25")).isEqualTo("-3.75");
        assertThat(result(Operation.ADD, "-5", "3")).isEqualTo("-2");
    }

    @Test
    void addsWithLargePrecisionWithoutRounding_BR3() {
        assertThat(result(
                        Operation.ADD, "12345678901234567890123456789012345678901234567890", "0.00000000000000000001"))
                .isEqualTo("12345678901234567890123456789012345678901234567890.00000000000000000001");
    }

    @Test
    void subtractsToZero() {
        assertThat(result(Operation.SUBTRACT, "1.0", "1.00")).isEqualTo("0");
    }

    @Test
    void subtractsToNegativeResult() {
        assertThat(result(Operation.SUBTRACT, "2", "5.5")).isEqualTo("-3.5");
    }

    @Test
    void multipliesByZeroToZero() {
        assertThat(result(Operation.MULTIPLY, "123.45", "0")).isEqualTo("0");
        assertThat(result(Operation.MULTIPLY, "-123.45", "0.00")).isEqualTo("0");
    }

    @Test
    void multipliesFollowingSignRules() {
        assertThat(result(Operation.MULTIPLY, "-2", "3")).isEqualTo("-6");
        assertThat(result(Operation.MULTIPLY, "2", "-3")).isEqualTo("-6");
        assertThat(result(Operation.MULTIPLY, "-2", "-3")).isEqualTo("6");
    }

    @Test
    void multipliesWithScaleGrowthExactly_BR3() {
        assertThat(result(Operation.MULTIPLY, "0.001", "0.0001")).isEqualTo("0.0000001");
        assertThat(result(Operation.MULTIPLY, "1.1", "1.1")).isEqualTo("1.21");
    }

    @Test
    void stripsTrailingZerosFromResult_BR4() {
        assertThat(new Calculation(Operation.ADD, new BigDecimal("1.50"), new BigDecimal("1.50")).result())
                .isEqualByComparingTo("3")
                .hasScaleOf(0);
    }

    @Test
    void returnsZeroWithNoScaleForEveryZeroResult_BR4() {
        assertThat(new Calculation(Operation.SUBTRACT, new BigDecimal("0.00"), new BigDecimal("-0")).result())
                .isEqualTo(BigDecimal.ZERO);
    }
}
