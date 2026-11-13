package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calculations")
class CalculationController {

    private static final String PROBLEM = "#/components/schemas/Problem";
    private static final String EXAMPLES = "#/components/examples/";

    private final PerformCalculation performCalculation;

    CalculationController(PerformCalculation performCalculation) {
        this.performCalculation = performCalculation;
    }

    // Problem schema and example bodies are components registered in config.OpenApiConfig, built from the domain.
    @Operation(summary = "Apply one Operation to its Operands and return the exact Result")
    @ApiResponse(
            responseCode = "200",
            description = "The Result; every number is a plain-notation string",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalculationResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (including b not matching the Operation's arity), unknown operation,"
                    + " or operand out of range",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = PROBLEM),
                            examples = {
                                @ExampleObject(name = "invalid-request", ref = EXAMPLES + "invalid-request"),
                                @ExampleObject(
                                        name = "invalid-request-missing-field",
                                        ref = EXAMPLES + "invalid-request-missing-field"),
                                @ExampleObject(
                                        name = "invalid-request-arity",
                                        ref = EXAMPLES + "invalid-request-arity"),
                                @ExampleObject(name = "unknown-operation", ref = EXAMPLES + "unknown-operation"),
                                @ExampleObject(name = "operand-out-of-range", ref = EXAMPLES + "operand-out-of-range")
                            }))
    @ApiResponse(
            responseCode = "406",
            description = "The client does not accept application/json",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = PROBLEM),
                            examples = @ExampleObject(name = "not-acceptable", ref = EXAMPLES + "not-acceptable")))
    @ApiResponse(
            responseCode = "415",
            description = "The body is not application/json",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = PROBLEM),
                            examples =
                                    @ExampleObject(
                                            name = "unsupported-media-type",
                                            ref = EXAMPLES + "unsupported-media-type")))
    @ApiResponse(
            responseCode = "422",
            description = "Valid input that violates a domain rule",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = PROBLEM),
                            examples = {
                                @ExampleObject(name = "division-by-zero", ref = EXAMPLES + "division-by-zero"),
                                @ExampleObject(name = "invalid-exponent", ref = EXAMPLES + "invalid-exponent"),
                                @ExampleObject(name = "negative-square-root", ref = EXAMPLES + "negative-square-root"),
                                @ExampleObject(name = "result-out-of-range", ref = EXAMPLES + "result-out-of-range")
                            }))
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected failure; no internals are exposed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = PROBLEM),
                            examples =
                                    @ExampleObject(name = "unexpected-failure", ref = EXAMPLES + "unexpected-failure")))
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        return CalculationResponse.from(performCalculation.perform(request.operation(), request.a(), request.b()));
    }
}
