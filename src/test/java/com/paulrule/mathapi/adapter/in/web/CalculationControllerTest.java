package com.paulrule.mathapi.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.config.JacksonConfig;
import com.paulrule.mathapi.config.UseCaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CalculationController.class)
@Import({UseCaseConfig.class, JacksonConfig.class})
class CalculationControllerTest {

    @Autowired
    MockMvc mockMvc;

    private ResultActions calculate(String body) throws Exception {
        return mockMvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static ResultActions problem(ResultActions actions, int status, String type) throws Exception {
        return actions.andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void calculatesWithStringOperands() throws Exception {
        calculate("""
                {"operation":"ADD","a":"0.1","b":"0.2"}""")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("ADD"))
                .andExpect(jsonPath("$.a").value("0.1"))
                .andExpect(jsonPath("$.b").value("0.2"))
                .andExpect(jsonPath("$.result").value("0.3"));
    }

    @Test
    void calculatesWithNumberOperandsAndEchoesThemAsReceived() throws Exception {
        calculate("""
                {"operation":"MULTIPLY","a":1e2,"b":1.50}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("100"))
                .andExpect(jsonPath("$.b").value("1.50"))
                .andExpect(jsonPath("$.result").value("150"));
    }

    @Test
    void returnsResultInPlainNotation() throws Exception {
        calculate("""
                {"operation":"MULTIPLY","a":"100","b":"10"}""").andExpect(jsonPath("$.result").value("1000"));
        calculate("""
                {"operation":"SUBTRACT","a":"0.0000001","b":"0"}""").andExpect(jsonPath("$.result").value("0.0000001"));
    }

    @Test
    void dividesToThirtyFourSignificantDigits() throws Exception {
        calculate("""
                {"operation":"DIVIDE","a":1,"b":"3"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0.3333333333333333333333333333333333"));
    }

    @Test
    void rejectsDivisionByZeroAsUnprocessable_BR2() throws Exception {
        problem(calculate("""
                {"operation":"DIVIDE","a":"1","b":0}"""), 422, "/problems/division-by-zero");
        problem(calculate("""
                {"operation":"DIVIDE","a":"1","b":"0.00"}"""), 422, "/problems/division-by-zero");
        problem(calculate("""
                {"operation":"DIVIDE","a":"1","b":-0}"""), 422, "/problems/division-by-zero");
    }

    @Test
    void raisesToAnIntegerPower() throws Exception {
        calculate("""
                {"operation":"POWER","a":2,"b":"-2"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b").value("-2"))
                .andExpect(jsonPath("$.result").value("0.25"));
    }

    @Test
    void takesTheTruncatedRemainder_BR7() throws Exception {
        calculate("""
                {"operation":"MODULO","a":"-7","b":3}""").andExpect(status().isOk()).andExpect(jsonPath("$.result").value("-1"));
    }

    @Test
    void rejectsInvalidExponentAsUnprocessable_BR5() throws Exception {
        problem(calculate("""
                {"operation":"POWER","a":2,"b":"0.5"}"""), 422, "/problems/invalid-exponent");
        problem(calculate("""
                {"operation":"POWER","a":2,"b":1000}"""), 422, "/problems/invalid-exponent");
    }

    @Test
    void rejectsZeroToANegativePowerAndModuloByZeroAsDivisionByZero_BR2() throws Exception {
        problem(calculate("""
                {"operation":"POWER","a":0,"b":-1}"""), 422, "/problems/division-by-zero");
        problem(calculate("""
                {"operation":"MODULO","a":1,"b":"0.00"}"""), 422, "/problems/division-by-zero");
    }

    @Test
    void rejectsResultOutOfRangeAsUnprocessable_BR6() throws Exception {
        problem(calculate("""
                {"operation":"POWER","a":100,"b":999}"""), 422, "/problems/result-out-of-range");
    }

    @Test
    void reportsOperandOutOfRangeBeforeInvalidExponent_BR1_BR5() throws Exception {
        problem(calculate("""
                {"operation":"POWER","a":"1E+1001","b":"0.5"}"""), 400, "/problems/operand-out-of-range");
    }

    @Test
    void rejectsOperandOutOfRangeNamingItButNotEchoingIt_BR1() throws Exception {
        problem(calculate("""
                {"operation":"MULTIPLY","a":1e999999999,"b":"1e999999999"}"""), 400, "/problems/operand-out-of-range")
                .andExpect(jsonPath("$.detail").value(containsString("Operand a")))
                .andExpect(jsonPath("$.detail").value(not(containsString("999999999"))));
        problem(calculate("""
                {"operation":"ADD","a":"1","b":"1E-1001"}"""), 400, "/problems/operand-out-of-range")
                .andExpect(jsonPath("$.detail").value(containsString("Operand b")));
    }

    @Test
    void acceptsOperandsAtTheLimits_BR1() throws Exception {
        calculate("""
                {"operation":"ADD","a":"1E+1000","b":1E-1000}""").andExpect(status().isOk());
    }

    @Test
    void reportsUnknownOperationBeforeOperandOutOfRange() throws Exception {
        problem(calculate("""
                {"operation":"NOT_AN_OPERATION","a":"1E+1001","b":"1"}"""), 400, "/problems/unknown-operation");
    }

    @Test
    void acceptsStringsAndNumbersOfOneThousandCharacters() throws Exception {
        var thousandCharacters = "0." + "0".repeat(997) + "1"; // scale 998, one significant digit
        calculate("{\"operation\":\"ADD\",\"a\":\"" + thousandCharacters + "\",\"b\":" + thousandCharacters + "}")
                .andExpect(status().isOk());
    }

    // 1,001 characters, scale 999: within BR-1, so only the parse limit can reject it.
    private static final String LONG_NUMBER = "0." + "0".repeat(998) + "1";

    @Test
    void rejectsStringOverOneThousandCharactersAsInvalid() throws Exception {
        problem(
                calculate("{\"operation\":\"ADD\",\"a\":\"" + LONG_NUMBER + "\",\"b\":1}"),
                400,
                "/problems/invalid-request");
    }

    @Test
    void rejectsNumberOverOneThousandCharactersAsInvalid() throws Exception {
        problem(
                calculate("{\"operation\":\"ADD\",\"a\":" + LONG_NUMBER + ",\"b\":1}"),
                400,
                "/problems/invalid-request");
    }

    @Test
    void rejectsFieldNameOverOneThousandCharactersAsInvalid() throws Exception {
        problem(
                calculate("{\"operation\":\"ADD\",\"a\":1,\"b\":2,\"" + "k".repeat(1_001) + "\":0}"),
                400,
                "/problems/invalid-request");
    }

    @Test
    void acceptsBodyOfSixteenThousandThreeHundredEightyFourBytes() throws Exception {
        calculate(padded(16_384)).andExpect(status().isOk());
    }

    @Test
    void rejectsBodyOverSixteenThousandThreeHundredEightyFourBytesAsInvalid() throws Exception {
        problem(calculate(padded(16_385)), 400, "/problems/invalid-request");
    }

    @Test
    void limitsBodyInUtf8BytesNotCharacters() throws Exception {
        // About 15,200 characters but 19,000 bytes: "é" is two bytes in UTF-8.
        var body = "{\"operation\":\"ADD\",\"a\":1,\"b\":2,\"x\":[" + "\"é\",".repeat(3_800) + "0]}";
        problem(
                mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(java.nio.charset.StandardCharsets.UTF_8))),
                400,
                "/problems/invalid-request");
    }

    /** A valid request padded with whitespace to exactly {@code length} characters. */
    private static String padded(int length) {
        var body = "{\"operation\":\"ADD\",\"a\":1,\"b\":2}";
        return body.substring(0, body.length() - 1) + " ".repeat(length - body.length()) + "}";
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        problem(calculate("{\"operation\":"), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsMissingField() throws Exception {
        problem(calculate("""
                {"operation":"ADD","a":"1"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"a":"1","b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":"ADD","a":null,"b":"2"}"""), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsNonNumericOperand() throws Exception {
        problem(calculate("""
                {"operation":"ADD","a":"abc","b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":"ADD","a":true,"b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":"ADD","a":{},"b":"2"}"""), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsNonStringOperation() throws Exception {
        problem(calculate("""
                {"operation":123,"a":"1","b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":true,"a":"1","b":"2"}"""), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsUnknownOperation() throws Exception {
        problem(calculate("""
                {"operation":"NOT_AN_OPERATION","a":"1","b":"2"}"""), 400, "/problems/unknown-operation")
                .andExpect(
                        jsonPath("$.detail").value(containsString("ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, MODULO")));
        problem(calculate("""
                {"operation":"add","a":"1","b":"2"}"""), 400, "/problems/unknown-operation");
    }

    @Test
    void reportsInvalidRequestBeforeUnknownOperation() throws Exception {
        problem(calculate("""
                {"operation":"NOT_AN_OPERATION","a":"abc","b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":"NOT_AN_OPERATION","a":"1"}"""), 400, "/problems/invalid-request");
    }

    @Test
    void reportsFrameworkErrorsAsProblemDetails() throws Exception {
        problem(
                mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("x")),
                415,
                "about:blank");
        problem(mockMvc.perform(get("/api/v1/calculations")), 405, "about:blank");
    }
}
