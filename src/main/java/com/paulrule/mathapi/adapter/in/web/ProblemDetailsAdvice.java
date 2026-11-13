package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.DivisionByZeroException;
import com.paulrule.mathapi.domain.OperandOutOfRangeException;
import com.paulrule.mathapi.domain.UnknownOperationException;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** The single place errors become RFC 9457 Problem Details. Framework errors come from the base class. */
@RestControllerAdvice
class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailsAdvice.class);

    @ExceptionHandler(UnknownOperationException.class)
    ProblemDetail unknownOperation(UnknownOperationException e) {
        return problem(HttpStatus.BAD_REQUEST, "unknown-operation", "Unknown operation", e.getMessage());
    }

    @ExceptionHandler(OperandOutOfRangeException.class)
    ProblemDetail operandOutOfRange(OperandOutOfRangeException e) {
        return problem(HttpStatus.BAD_REQUEST, "operand-out-of-range", "Operand out of range", e.getMessage());
    }

    @ExceptionHandler(DivisionByZeroException.class)
    ProblemDetail divisionByZero(DivisionByZeroException e) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "division-by-zero", "Division by zero", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Unexpected failure", e);
        var problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setType(ABOUT_BLANK);
        return problem;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // The parser's message can echo input and internals, so it is replaced, not passed on.
        return invalidRequest("The request body is not valid JSON or an operand is not a number.");
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return invalidRequest("Missing required field(s): operation, a and b are all required.");
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        var response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        // PRD §3.2: every error carries a type; Spring 7 leaves it null (omitted) by default.
        if (response != null && response.getBody() instanceof ProblemDetail problem && problem.getType() == null) {
            problem.setType(ABOUT_BLANK);
        }
        return response;
    }

    private static ResponseEntity<Object> invalidRequest(String detail) {
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", detail));
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + type));
        problem.setTitle(title);
        return problem;
    }
}
