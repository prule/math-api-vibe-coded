package com.paulrule.mathapi.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paulrule.mathapi.domain.Calculation;
import io.swagger.v3.oas.annotations.media.Schema;

/** Every number is a plain-notation string so clients lose no precision. */
@Schema(requiredProperties = {"operation", "a", "result"}) // b is absent for a unary Operation (BR-9)
public record CalculationResponse(
        String operation,
        String a,

        @JsonInclude(JsonInclude.Include.NON_NULL) @Schema(description = "Absent for a unary Operation")
        String b,

        String result) {

    static CalculationResponse from(Calculation calculation) {
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b() == null ? null : calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
