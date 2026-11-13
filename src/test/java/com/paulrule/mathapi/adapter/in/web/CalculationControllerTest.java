package com.paulrule.mathapi.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
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
                {"operation":"POWER","a":"1","b":"2"}"""), 400, "/problems/unknown-operation")
                .andExpect(jsonPath("$.detail").value(containsString("ADD, SUBTRACT, MULTIPLY")));
        problem(calculate("""
                {"operation":"add","a":"1","b":"2"}"""), 400, "/problems/unknown-operation");
    }

    @Test
    void reportsInvalidRequestBeforeUnknownOperation() throws Exception {
        problem(calculate("""
                {"operation":"POWER","a":"abc","b":"2"}"""), 400, "/problems/invalid-request");
        problem(calculate("""
                {"operation":"POWER","a":"1"}"""), 400, "/problems/invalid-request");
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
