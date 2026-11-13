package com.paulrule.mathapi.config;

import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;

@Configuration
public class JacksonConfig {

    /**
     * PRD §3.2 request limits, enforced by the parser: a body over 16,384 bytes, or any string or field name over
     * 1,000 characters, is an invalid request. Operand numbers are capped by {@code OperandDeserializer}.
     */
    @Bean
    JsonFactoryBuilderCustomizer requestLimits() {
        return builder -> builder.streamReadConstraints(StreamReadConstraints.builder()
                .maxDocumentLength(16_384)
                .maxStringLength(1_000)
                .maxNameLength(1_000)
                .build());
    }

    /** Jackson turns {"operation": 123} into "123" by default; PRD §3.2 wants a non-string rejected as invalid. */
    @Bean
    JsonMapperBuilderCustomizer rejectScalarsForStrings() {
        return builder -> builder.withCoercionConfig(
                String.class,
                config -> config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
