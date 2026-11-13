package com.paulrule.mathapi.config;

import com.paulrule.mathapi.adapter.in.web.ProblemType;
import com.paulrule.mathapi.domain.ArityMismatchException;
import com.paulrule.mathapi.domain.DivisionByZeroException;
import com.paulrule.mathapi.domain.InvalidExponentException;
import com.paulrule.mathapi.domain.NegativeSquareRootException;
import com.paulrule.mathapi.domain.OperandOutOfRangeException;
import com.paulrule.mathapi.domain.Operation;
import com.paulrule.mathapi.domain.ResultOutOfRangeException;
import com.paulrule.mathapi.domain.UnknownOperationException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Documented values that come from the domain are generated here, never copied into annotations (STANDARDS §4).
 * Example bodies are built by the same {@link ProblemType} and exception messages the API answers with.
 */
@Configuration
public class OpenApiConfig {

    private static final String INSTANCE = "/api/v1/calculations";

    @Bean
    OpenApiCustomizer domainDocumentation() {
        return openApi -> {
            Components components = openApi.getComponents();
            @SuppressWarnings("unchecked") // swagger-models exposes properties as raw Schema
            var operation = (Schema<String>) components
                    .getSchemas()
                    .get("CalculationRequest")
                    .getProperties()
                    .get("operation");
            operation.setEnum(Operation.names());
            operation.setExample(Operation.DIVIDE.name());

            components.addSchemas("Problem", problemSchema());
            examples().forEach(components::addExamples);
        };
    }

    /** What a Problem Details body really contains; springdoc's ProblemDetail schema adds a phantom field. */
    private static Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("RFC 9457 Problem Details")
                .addProperty("type", new StringSchema().description("Stable problem type URI, or about:blank"))
                .addProperty("title", new StringSchema())
                .addProperty("status", new IntegerSchema())
                .addProperty("detail", new StringSchema().description("Human-readable; never exposes internals"))
                .addProperty("instance", new StringSchema().description("The request path"))
                .required(List.of("type", "title", "status", "detail"));
    }

    private static Map<String, Example> examples() {
        var examples = new LinkedHashMap<String, Example>();
        examples.put("invalid-request", example(ProblemType.INVALID_REQUEST.problem(ProblemType.UNREADABLE_DETAIL)));
        examples.put(
                "invalid-request-missing-field",
                example(ProblemType.INVALID_REQUEST.problem(ProblemType.MISSING_FIELD_DETAIL)));
        examples.put(
                "invalid-request-arity",
                example(ProblemType.INVALID_REQUEST.problem(new ArityMismatchException(Operation.SQRT).getMessage())));
        examples.put(
                ProblemType.UNKNOWN_OPERATION.slug(),
                example(ProblemType.UNKNOWN_OPERATION.problem(new UnknownOperationException().getMessage())));
        examples.put(
                ProblemType.OPERAND_OUT_OF_RANGE.slug(),
                example(ProblemType.OPERAND_OUT_OF_RANGE.problem(new OperandOutOfRangeException("a").getMessage())));
        examples.put(
                ProblemType.DIVISION_BY_ZERO.slug(),
                example(ProblemType.DIVISION_BY_ZERO.problem(new DivisionByZeroException().getMessage())));
        examples.put(
                ProblemType.INVALID_EXPONENT.slug(),
                example(ProblemType.INVALID_EXPONENT.problem(new InvalidExponentException().getMessage())));
        examples.put(
                ProblemType.NEGATIVE_SQUARE_ROOT.slug(),
                example(ProblemType.NEGATIVE_SQUARE_ROOT.problem(new NegativeSquareRootException().getMessage())));
        examples.put(
                ProblemType.RESULT_OUT_OF_RANGE.slug(),
                example(ProblemType.RESULT_OUT_OF_RANGE.problem(new ResultOutOfRangeException().getMessage())));
        // Framework errors: Spring writes these details; shown as returned for a typical request.
        examples.put(
                "unsupported-media-type",
                example(ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Content-Type 'text/plain;charset=UTF-8' is not supported.")));
        examples.put(
                "not-acceptable",
                example(ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_ACCEPTABLE, "Acceptable representations: [application/json].")));
        examples.put(
                "unexpected-failure",
                example(ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR, ProblemType.UNEXPECTED_DETAIL)));
        return examples;
    }

    private static Example example(ProblemDetail problem) {
        var value = new LinkedHashMap<String, Object>();
        value.put(
                "type",
                problem.getType() == null ? "about:blank" : problem.getType().toString());
        value.put("title", problem.getTitle());
        value.put("status", problem.getStatus());
        value.put("detail", problem.getDetail());
        value.put("instance", INSTANCE);
        return new Example().value(value);
    }
}
