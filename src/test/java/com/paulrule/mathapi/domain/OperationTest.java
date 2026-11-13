package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OperationTest {

    @Test
    void findsOperationByExactName() {
        assertThat(Operation.named("ADD")).isEqualTo(Operation.ADD);
        assertThat(Operation.named("SUBTRACT")).isEqualTo(Operation.SUBTRACT);
        assertThat(Operation.named("MULTIPLY")).isEqualTo(Operation.MULTIPLY);
    }

    @Test
    void rejectsNameInWrongCase() {
        assertThatThrownBy(() -> Operation.named("add")).isInstanceOf(UnknownOperationException.class);
    }

    @Test
    void rejectsUnsupportedName() {
        assertThatThrownBy(() -> Operation.named("POWER")).isInstanceOf(UnknownOperationException.class);
        assertThatThrownBy(() -> Operation.named("")).isInstanceOf(UnknownOperationException.class);
    }
}
