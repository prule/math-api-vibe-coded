package com.paulrule.mathapi.domain;

/** BR-9: the Operands given don't match the Operation's arity. */
public class ArityMismatchException extends RuntimeException {

    public ArityMismatchException(Operation operation) {
        super(
                operation.arity() == 1
                        ? operation + " takes operand a only; omit b."
                        : operation + " takes operands a and b; both are required.");
    }
}
