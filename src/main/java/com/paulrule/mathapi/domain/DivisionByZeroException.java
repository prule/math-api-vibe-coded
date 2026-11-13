package com.paulrule.mathapi.domain;

public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Division by zero is undefined.");
    }
}
