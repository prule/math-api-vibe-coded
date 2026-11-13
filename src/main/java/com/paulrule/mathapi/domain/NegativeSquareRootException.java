package com.paulrule.mathapi.domain;

/** BR-8: the square root of an Operand below zero. */
public class NegativeSquareRootException extends RuntimeException {

    public NegativeSquareRootException() {
        super("The square root of a negative operand is not a real number.");
    }
}
