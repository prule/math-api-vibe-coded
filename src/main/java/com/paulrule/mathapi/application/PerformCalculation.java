package com.paulrule.mathapi.application;

import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import java.math.BigDecimal;

/** Use case: perform one Calculation named by the client. */
public class PerformCalculation {

    public Calculation perform(String operation, BigDecimal a, BigDecimal b) {
        return new Calculation(Operation.named(operation), a, b);
    }
}
