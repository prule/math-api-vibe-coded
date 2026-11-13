package com.paulrule.mathapi.adapter.in.web;

import java.math.BigDecimal;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Reads an Operand from a JSON number or string. PRD §3.2 caps each at 1,000 characters before it becomes a number;
 * Jackson's own number limit counts digits, not characters ({@code 0.} is free), so the cap is checked here.
 */
class OperandDeserializer extends ValueDeserializer<BigDecimal> {

    static final int MAX_LENGTH = 1_000;

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken token = p.currentToken();
        if (token != JsonToken.VALUE_STRING && !token.isNumeric()) {
            return (BigDecimal) ctxt.handleUnexpectedToken(BigDecimal.class, p);
        }
        String text = p.getString();
        if (text.length() > MAX_LENGTH) { // strings are already capped by the parser; this catches numbers
            throw ctxt.weirdStringException("", BigDecimal.class, "longer than " + MAX_LENGTH + " characters");
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw ctxt.weirdStringException("", BigDecimal.class, "not a number");
        }
    }
}
