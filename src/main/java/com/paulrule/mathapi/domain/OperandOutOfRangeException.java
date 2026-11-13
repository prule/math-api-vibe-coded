package com.paulrule.mathapi.domain;

/** Names the Operand but never echoes its value, which may be enormous (BR-1). */
public class OperandOutOfRangeException extends RuntimeException {

    public OperandOutOfRangeException(String operandName) {
        super("Operand " + operandName + " is out of range: at most " + Calculation.MAX_PRECISION
                + " significant digits and a scale within ±" + Calculation.MAX_SCALE + ".");
    }
}
