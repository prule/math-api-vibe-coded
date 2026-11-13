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
        return ProblemType.UNKNOWN_OPERATION.problem(e.getMessage());
    }

    @ExceptionHandler(OperandOutOfRangeException.class)
    ProblemDetail operandOutOfRange(OperandOutOfRangeException e) {
        return ProblemType.OPERAND_OUT_OF_RANGE.problem(e.getMessage());
    }

    @ExceptionHandler(DivisionByZeroException.class)
    ProblemDetail divisionByZero(DivisionByZeroException e) {
        return ProblemType.DIVISION_BY_ZERO.problem(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Unexpected failure", e);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ProblemType.UNEXPECTED_DETAIL);
        problem.setType(ABOUT_BLANK);
        return problem;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // The parser's message can echo input and internals, so it is replaced, not passed on.
        return invalidRequest(ProblemType.UNREADABLE_DETAIL);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return invalidRequest(ProblemType.MISSING_FIELD_DETAIL);
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
        return ResponseEntity.badRequest().body(ProblemType.INVALID_REQUEST.problem(detail));
    }
}
