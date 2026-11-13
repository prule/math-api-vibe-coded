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

    @Test
    void raisesToAPositiveIntegerPowerExactly() {
        assertThat(result(Operation.POWER, "2", "10")).isEqualTo("1024");
        assertThat(result(Operation.POWER, "1.5", "2")).isEqualTo("2.25");
    }

    @Test
    void raisesToANegativePowerAsTheReciprocal_BR5() {
        assertThat(result(Operation.POWER, "2", "-2")).isEqualTo("0.25");
        assertThat(result(Operation.POWER, "3", "-1")).isEqualTo("0." + "3".repeat(34));
    }

    @Test
    void raisesANegativeBaseFollowingSignRules() {
        assertThat(result(Operation.POWER, "-2", "3")).isEqualTo("-8");
        assertThat(result(Operation.POWER, "-2", "2")).isEqualTo("4");
    }

    @Test
    void roundsALargePowerOnceToThirtyFourSignificantDigits_BR3() {
        // 2^200 = 1606938044258990275541962092341162602522202993782792835301376 (61 digits)
        assertThat(result(Operation.POWER, "2", "200"))
                .isEqualTo("1606938044258990275541962092341163" + "0".repeat(27));
    }

    @Test
    void roundsThePowerOnceNotAtEachMultiplication_BR3() {
        // 1.0000000000000000000000000000000001^999 is 1 + 999E-34 + ..., which rounds to 1 + 1E-31;
        // rounding at each step would round the base to 1 and return 1.
        assertThat(result(Operation.POWER, "1.0000000000000000000000000000000001", "999"))
                .isEqualTo("1." + "0".repeat(30) + "1");
        // BigDecimal.pow(668, DECIMAL128) answers ...838 here: it may be off in the last digit, the exact power isn't.
        assertThat(new Calculation(
                                Operation.POWER,
                                new BigDecimal("0.2857142857142857142857142857142857"),
                                new BigDecimal("668"))
                        .result())
                .isEqualByComparingTo("3.652131226730419500116141828190839E-364");
    }

    @Test
    void raisesEveryBaseToTheZerothPowerAsOne_BR5() {
        assertThat(result(Operation.POWER, "0", "0")).isEqualTo("1");
        assertThat(result(Operation.POWER, "-7.5", "0.00")).isEqualTo("1");
    }

    @Test
    void acceptsAnIntegerExponentWrittenWithAScale_BR5() {
        assertThat(result(Operation.POWER, "2", "3.0")).isEqualTo("8");
        assertThat(result(Operation.POWER, "10", "1E+2")).isEqualTo("1" + "0".repeat(100));
    }

    @Test
    void acceptsExponentsAtTheLimit_BR5() {
        assertThat(result(Operation.POWER, "1", "999")).isEqualTo("1");
        assertThat(result(Operation.POWER, "1", "-999")).isEqualTo("1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "1000", "-1000", "1E+1000", "999.0000001"})
    void rejectsAnExponentThatIsNotAnIntegerWithinNineHundredNinetyNine_BR5(String exponent) {
        var calculation = new Calculation(Operation.POWER, new BigDecimal("2"), new BigDecimal(exponent));

        assertThatThrownBy(calculation::result).isInstanceOf(InvalidExponentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0"})
    void rejectsZeroToANegativePowerAsDivisionByZero_BR2(String zero) {
        var calculation = new Calculation(Operation.POWER, new BigDecimal(zero), new BigDecimal("-1"));

        assertThatThrownBy(calculation::result).isInstanceOf(DivisionByZeroException.class);
    }

    @Test
    void raisesZeroToAPositivePowerAsZero() {
        assertThat(result(Operation.POWER, "0.00", "5")).isEqualTo("0");
    }

    @Test
    void checksTheExponentBeforeDivisionByZero_BR5_BR2() {
        var calculation = new Calculation(Operation.POWER, BigDecimal.ZERO, new BigDecimal("-0.5"));

        assertThatThrownBy(calculation::result).isInstanceOf(InvalidExponentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "0.01"})
    void rejectsAPowerWhoseResultScaleIsOutsideOneThousand_BR6(String base) {
        var calculation = new Calculation(Operation.POWER, new BigDecimal(base), new BigDecimal("999"));

        assertThatThrownBy(calculation::result).isInstanceOf(ResultOutOfRangeException.class);
    }

    @Test
    void acceptsAPowerWhoseResultScaleIsAtTheLimit_BR6() {
        assertThat(result(Operation.POWER, "10", "-999")).isEqualTo("0." + "0".repeat(998) + "1");
        assertThat(result(Operation.POWER, "1E+10", "100")).isEqualTo("1" + "0".repeat(1000));
        assertThat(result(Operation.POWER, "1E-10", "100")).isEqualTo("0." + "0".repeat(999) + "1");
    }

    @Test
    void rejectsAPowerWhoseResultScaleIsOnePastTheLimit_BR6() {
        var calculation = new Calculation(Operation.POWER, new BigDecimal("1E+7"), new BigDecimal("143"));

        assertThatThrownBy(calculation::result).isInstanceOf(ResultOutOfRangeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"999", "-999"})
    void raisesTheLargestOperandToTheLargestExponentWithinOneSecond_BR5(String exponent) {
        var largest = new BigDecimal("9." + "9".repeat(99));

        assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> new Calculation(Operation.POWER, largest, new BigDecimal(exponent)).result());
    }

    @Test
    void takesTheTruncatedRemainderWithTheSignOfTheDividend_BR7() {
        assertThat(result(Operation.MODULO, "-7", "3")).isEqualTo("-1");
        assertThat(result(Operation.MODULO, "7", "-3")).isEqualTo("1");
        assertThat(result(Operation.MODULO, "7", "3")).isEqualTo("1");
    }

    @Test
    void takesTheRemainderOfDecimalsExactly_BR7() {
        assertThat(result(Operation.MODULO, "5.5", "2")).isEqualTo("1.5");
        assertThat(result(Operation.MODULO, "-7.5", "2")).isEqualTo("-1.5");
    }

    @Test
    void takesAZeroRemainderAsZero() {
        assertThat(result(Operation.MODULO, "6", "3")).isEqualTo("0");
    }

    @Test
    void takesTheRemainderOfTheLargestOperandExactly() {
        assertThat(result(Operation.MODULO, "1E+1000", "7")).isEqualTo("4");
    }

    @Test
    void takesTheRemainderOfTheWidestOperandsWithinOneSecond() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> new Calculation(
                                Operation.MODULO,
                                new BigDecimal("9." + "9".repeat(99) + "E+1000"),
                                new BigDecimal("7E-1000"))
                        .result());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0"})
    void rejectsModuloByEveryZeroRepresentation_BR2(String zero) {
        var calculation = new Calculation(Operation.MODULO, BigDecimal.ONE, new BigDecimal(zero));

        assertThatThrownBy(calculation::result).isInstanceOf(DivisionByZeroException.class);
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
