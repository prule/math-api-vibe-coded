package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Operands arrive as JSON numbers or numeric strings; Jackson parses both straight to BigDecimal. */
public record CalculationRequest(
        @NotNull String operation,
        @NotNull BigDecimal a,
        @NotNull BigDecimal b) {}
