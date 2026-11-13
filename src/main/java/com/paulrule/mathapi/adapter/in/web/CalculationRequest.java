package com.paulrule.mathapi.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonDeserialize;

public record CalculationRequest(
        @NotNull @Schema(description = "Operation name, case-sensitive")
        String operation,

        @NotNull
        @JsonDeserialize(using = OperandDeserializer.class)
        @Schema(
                types = {"number", "string"},
                description = "Operand as a JSON number or numeric string",
                example = "1")
        BigDecimal a,

        @NotNull
        @JsonDeserialize(using = OperandDeserializer.class)
        @Schema(
                types = {"number", "string"},
                description = "Operand as a JSON number or numeric string",
                example = "3")
        BigDecimal b) {}
