package com.paulrule.mathapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Under the prod profile the API docs and Swagger UI are not served (PRD §3.3, STANDARDS §8). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ApiDocumentationDisabledTest {

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/v3/api-docs",
                "/v3/api-docs/swagger-config",
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/webjars/swagger-ui/index.html"
            })
    void doesNotServeApiDocs(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("about:blank"));
    }

    @Test
    void stillServesTheApiWithStaticResourcesOff() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operation\":\"ADD\",\"a\":1,\"b\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("3"));
    }
}
