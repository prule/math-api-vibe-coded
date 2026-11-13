package com.paulrule.mathapi.domain;

public class ResultOutOfRangeException extends RuntimeException {

    public ResultOutOfRangeException() {
        super("The result is out of range: its scale must be within ±" + Calculation.MAX_SCALE + ".");
    }
}
