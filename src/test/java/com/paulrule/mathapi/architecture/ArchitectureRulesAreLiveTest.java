package com.paulrule.mathapi.architecture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paulrule.mathapi.domain.fixture.UsesSpring;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/** Proves the domain rule fails on a violation, since main code has no domain classes yet to catch one. */
class ArchitectureRulesAreLiveTest {

    @Test
    void domainRuleRejectsSpringImport() {
        var violator = new ClassFileImporter().importClasses(UsesSpring.class);

        assertThatThrownBy(() -> ArchitectureTest.DOMAIN_IS_PLAIN_JAVA.check(violator))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("UsesSpring");
    }
}
