package com.paulrule.mathapi.adapter.in.web;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * The PRD §3.2 Problem Details types. The one source for each type's status, URI and title, read by
 * {@link ProblemDetailsAdvice} to answer and by {@code config.OpenApiConfig} to document.
 */
public enum ProblemType {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request"),
    UNKNOWN_OPERATION(HttpStatus.BAD_REQUEST, "unknown-operation", "Unknown operation"),
    OPERAND_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "operand-out-of-range", "Operand out of range"),
    DIVISION_BY_ZERO(HttpStatus.UNPROCESSABLE_CONTENT, "division-by-zero", "Division by zero"),
    INVALID_EXPONENT(HttpStatus.UNPROCESSABLE_CONTENT, "invalid-exponent", "Invalid exponent"),
    RESULT_OUT_OF_RANGE(HttpStatus.UNPROCESSABLE_CONTENT, "result-out-of-range", "Result out of range");

    public static final String UNREADABLE_DETAIL = "The request body is not valid JSON or an operand is not a number.";
    public static final String MISSING_FIELD_DETAIL = "Missing required field(s): operation, a and b are all required.";
    public static final String UNEXPECTED_DETAIL = "An unexpected error occurred.";

    private final HttpStatus status;
    private final String slug;
    private final String title;

    ProblemType(HttpStatus status, String slug, String title) {
        this.status = status;
        this.slug = slug;
        this.title = title;
    }

    public String slug() {
        return slug;
    }

    public ProblemDetail problem(String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + slug));
        problem.setTitle(title);
        return problem;
    }
}
