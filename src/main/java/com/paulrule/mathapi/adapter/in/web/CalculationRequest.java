package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonDeserialize;

public record CalculationRequest(
        @NotNull String operation,

        @NotNull @JsonDeserialize(using = OperandDeserializer.class)
        BigDecimal a,

        @NotNull @JsonDeserialize(using = OperandDeserializer.class)
        BigDecimal b) {}
