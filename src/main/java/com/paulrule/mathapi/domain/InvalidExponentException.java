package com.paulrule.mathapi.domain;

public class InvalidExponentException extends RuntimeException {

    public InvalidExponentException() {
        super("The exponent must be an integer between -" + Operation.MAX_EXPONENT + " and " + Operation.MAX_EXPONENT
                + ".");
    }
}
