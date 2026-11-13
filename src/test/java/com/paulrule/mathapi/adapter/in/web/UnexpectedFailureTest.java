package com.paulrule.mathapi.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.application.PerformCalculation;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CalculationController.class)
class UnexpectedFailureTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PerformCalculation performCalculation;

    @Test
    void reportsUnexpectedFailureWithoutInternals() throws Exception {
        given(performCalculation.perform(any(), any(), any())).willThrow(new IllegalStateException("secret internals"));

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operation\":\"ADD\",\"a\":\"1\",\"b\":\"2\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value(Matchers.not(Matchers.containsString("secret"))));
    }
}
