package com.paulrule.mathapi.domain.fixture;

import org.springframework.context.ApplicationContext;

/** Deliberate violation of the domain rule, used only by ArchitectureRulesAreLiveTest. Not a bean. */
public class UsesSpring {
    ApplicationContext context;
}
