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
        assertThat(Operation.named("DIVIDE")).isEqualTo(Operation.DIVIDE);
        assertThat(Operation.named("POWER")).isEqualTo(Operation.POWER);
        assertThat(Operation.named("MODULO")).isEqualTo(Operation.MODULO);
    }

    @Test
    void rejectsNameInWrongCase() {
        assertThatThrownBy(() -> Operation.named("add")).isInstanceOf(UnknownOperationException.class);
    }

    @Test
    void rejectsUnsupportedName() {
        assertThatThrownBy(() -> Operation.named("NOT_AN_OPERATION")).isInstanceOf(UnknownOperationException.class);
        assertThatThrownBy(() -> Operation.named("")).isInstanceOf(UnknownOperationException.class);
    }
}
