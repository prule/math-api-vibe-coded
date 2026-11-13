package com.paulrule.mathapi.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;

@Configuration
public class JacksonConfig {

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
