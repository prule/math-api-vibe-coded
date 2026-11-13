package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.Calculation;

/** Every number is a plain-notation string so clients lose no precision. */
public record CalculationResponse(String operation, String a, String b, String result) {

    static CalculationResponse from(Calculation calculation) {
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
