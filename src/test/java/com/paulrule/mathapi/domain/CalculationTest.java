package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    void dividesOneByThreeToThirtyFourSignificantDigits_BR3() {
        assertThat(result(Operation.DIVIDE, "1", "3")).isEqualTo("0." + "3".repeat(34));
    }

    @Test
    void roundsDivisionHalfEven_BR3() {
        assertThat(result(Operation.DIVIDE, "2", "3")).isEqualTo("0." + "6".repeat(33) + "7");
        // 35 significant digits ending in an exact half: ties go to the even neighbour.
        assertThat(result(Operation.DIVIDE, "1234567890123456789012345678901234.5", "1"))
                .isEqualTo("1234567890123456789012345678901234");
        assertThat(result(Operation.DIVIDE, "1234567890123456789012345678901235.5", "1"))
                .isEqualTo("1234567890123456789012345678901236");
    }

    @Test
    void dividesExactlyWhenTheResultTerminates() {
        assertThat(result(Operation.DIVIDE, "1", "4")).isEqualTo("0.25");
        assertThat(result(Operation.DIVIDE, "10", "2")).isEqualTo("5");
        assertThat(result(Operation.DIVIDE, "100", "0.01")).isEqualTo("10000");
    }

    @Test
    void dividesByNegativeDivisor() {
        assertThat(result(Operation.DIVIDE, "1", "-4")).isEqualTo("-0.25");
        assertThat(result(Operation.DIVIDE, "-9", "-3")).isEqualTo("3");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0", "0E+5"})
    void rejectsDivisionByEveryZeroRepresentation_BR2(String zero) {
        var calculation = new Calculation(Operation.DIVIDE, BigDecimal.ONE, new BigDecimal(zero));

        assertThatThrownBy(calculation::result).isInstanceOf(DivisionByZeroException.class);
    }

    @Test
    void dividesZeroByNonZeroToZero() {
        assertThat(result(Operation.DIVIDE, "0.00", "7")).isEqualTo("0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E+1000", "1E-1000", "-1E+1000", "0E-1000"})
    void acceptsOperandsAtTheScaleLimits_BR1(String operand) {
        assertThatCode(() -> new Calculation(Operation.ADD, new BigDecimal(operand), BigDecimal.ONE))
                .doesNotThrowAnyException();
        assertThatCode(() -> new Calculation(Operation.ADD, BigDecimal.ONE, new BigDecimal(operand)))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsOperandWithOneHundredSignificantDigits_BR1() {
        var hundredDigits = new BigDecimal("9".repeat(100));

        assertThatCode(() -> new Calculation(Operation.MULTIPLY, hundredDigits, hundredDigits))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E+1001", "1E-1001", "0E+1001", "0E-1001"})
    void rejectsOperandOnePastTheScaleLimits_BR1(String operand) {
        assertThatThrownBy(() -> new Calculation(Operation.ADD, new BigDecimal(operand), BigDecimal.ONE))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessageContaining("Operand a")
                .hasMessageNotContaining("1001");
        assertThatThrownBy(() -> new Calculation(Operation.ADD, BigDecimal.ONE, new BigDecimal(operand)))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessageContaining("Operand b");
    }

    @Test
    void rejectsOperandWithOneHundredAndOneSignificantDigits_BR1() {
        // Small value, but 101 digits as written: the written form is what BR-1 bounds.
        var written = new BigDecimal("1" + "0".repeat(100));

        assertThatThrownBy(() -> new Calculation(Operation.ADD, written, BigDecimal.ONE))
                .isInstanceOf(OperandOutOfRangeException.class);
    }

    @Test
    void rejectsHugeExponentBeforeAnyArithmetic_BR1() {
        var huge = new BigDecimal("1e999999999");

        assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> assertThatThrownBy(() -> new Calculation(Operation.MULTIPLY, huge, huge))
                        .isInstanceOf(OperandOutOfRangeException.class));
    }

    @Test
    void checksOperandRangeBeforeDivisionByZero_BR1_BR2() {
        assertThatThrownBy(() -> new Calculation(Operation.DIVIDE, new BigDecimal("1E+1001"), BigDecimal.ZERO))
                .isInstanceOf(OperandOutOfRangeException.class);
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
