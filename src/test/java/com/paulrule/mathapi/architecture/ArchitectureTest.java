package com.paulrule.mathapi.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the STANDARDS.md §2 dependency rules.
 *
 * <p>shortcut: every rule allows empty packages (withOptionalLayers / allowEmptyShould), remove both once every
 * layer has a class (backlog item 2).
 */
@AnalyzeClasses(packages = "com.paulrule.mathapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule LAYERS_DEPEND_INWARDS = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Domain")
            .definedBy("..mathapi.domain..")
            .layer("Application")
            .definedBy("..mathapi.application..")
            .layer("Web")
            .definedBy("..mathapi.adapter.in.web..")
            .layer("Config")
            .definedBy("..mathapi.config..")
            .whereLayer("Web")
            .mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Web", "Config")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Application", "Web", "Config");

    @ArchTest
    static final ArchRule DOMAIN_IS_PLAIN_JAVA = classes()
            .that()
            .resideInAPackage("..mathapi.domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "..mathapi.domain..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule APPLICATION_HAS_NO_WEB_OR_ADAPTERS = noClasses()
            .that()
            .resideInAPackage("..mathapi.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework.web..",
                    "org.springframework.http..",
                    "org.springframework.boot.webmvc..",
                    "..mathapi.adapter..")
            .allowEmptyShould(true);
}
