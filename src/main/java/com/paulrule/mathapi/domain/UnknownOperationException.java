package com.paulrule.mathapi.domain;

public class UnknownOperationException extends RuntimeException {

    public UnknownOperationException() {
        super("Unknown operation. Supported operations: " + Operation.supportedNames() + ".");
    }
}
