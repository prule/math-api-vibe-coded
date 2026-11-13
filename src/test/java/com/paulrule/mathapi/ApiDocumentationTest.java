package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.paulrule.mathapi.domain.Operation;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** The generated OpenAPI docs describe POST /api/v1/calculations completely (STANDARDS §4). */
@SpringBootTest
@AutoConfigureMockMvc
class ApiDocumentationTest {

    private static final String POST = "$.paths['/api/v1/calculations'].post";
    private static final String PROBLEM = "['application/problem+json']";
    private static final String EXAMPLES = "$.components.examples";
    private static final String REF = "#/components/examples/";

    @Autowired
    MockMvc mockMvc;

    private ResultActions apiDocs() throws Exception {
        return mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private String exampleRefs(String status) {
        return POST + ".responses['" + status + "'].content" + PROBLEM + ".examples.*.$ref";
    }

    @Test
    void servesOpenApi31() throws Exception {
        apiDocs().andExpect(jsonPath("$.openapi").value(containsString("3.1")));
    }

    @Test
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    @Test
    void documentsSuccessAsJsonWithStringValues() throws Exception {
        apiDocs()
                .andExpect(jsonPath(POST + ".responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/CalculationResponse"))
                .andExpect(jsonPath("$.components.schemas.CalculationResponse.properties.*.type")
                        .value(containsInAnyOrder("string", "string", "string", "string")));
    }

    @Test
    void documentsOperandsAsNumberOrString() throws Exception {
        apiDocs()
                .andExpect(jsonPath("$.components.schemas.CalculationRequest.properties.a.type")
                        .value(containsInAnyOrder("number", "string")))
                .andExpect(jsonPath("$.components.schemas.CalculationRequest.properties.b.type")
                        .value(containsInAnyOrder("number", "string")))
                .andExpect(jsonPath("$.components.schemas.CalculationRequest.required")
                        .value(containsInAnyOrder("operation", "a", "b")));
    }

    @Test
    void documentsAllowedOperationsFromTheDomain() throws Exception {
        apiDocs()
                .andExpect(jsonPath("$.components.schemas.CalculationRequest.properties.operation.enum")
                        .value(containsInAnyOrder(Operation.names().toArray())));
    }

    @Test
    void documentsEveryResponseStatusWithItsProblemExamples() throws Exception {
        apiDocs()
                .andExpect(jsonPath(exampleRefs("400"))
                        .value(containsInAnyOrder(
                                REF + "invalid-request",
                                REF + "invalid-request-missing-field",
                                REF + "unknown-operation",
                                REF + "operand-out-of-range")))
                .andExpect(jsonPath(exampleRefs("406")).value(containsInAnyOrder(REF + "not-acceptable")))
                .andExpect(jsonPath(exampleRefs("415")).value(containsInAnyOrder(REF + "unsupported-media-type")))
                .andExpect(jsonPath(exampleRefs("422")).value(containsInAnyOrder(REF + "division-by-zero")))
                .andExpect(jsonPath(exampleRefs("500")).value(containsInAnyOrder(REF + "unexpected-failure")));
    }

    @Test
    void documentsEachProblemExampleWithItsTypeAndStatus() throws Exception {
        apiDocs()
                .andExpect(jsonPath(EXAMPLES + "['invalid-request'].value.type").value("/problems/invalid-request"))
                .andExpect(jsonPath(EXAMPLES + "['invalid-request-missing-field'].value.type")
                        .value("/problems/invalid-request"))
                .andExpect(
                        jsonPath(EXAMPLES + "['unknown-operation'].value.type").value("/problems/unknown-operation"))
                .andExpect(jsonPath(EXAMPLES + "['operand-out-of-range'].value.type")
                        .value("/problems/operand-out-of-range"))
                .andExpect(
                        jsonPath(EXAMPLES + "['division-by-zero'].value.type").value("/problems/division-by-zero"))
                .andExpect(
                        jsonPath(EXAMPLES + "['division-by-zero'].value.status").value(422))
                .andExpect(jsonPath(EXAMPLES + "['unsupported-media-type'].value.type")
                        .value("about:blank"))
                .andExpect(jsonPath(EXAMPLES + "['unsupported-media-type'].value.status")
                        .value(415))
                .andExpect(
                        jsonPath(EXAMPLES + "['not-acceptable'].value.status").value(406))
                .andExpect(jsonPath(EXAMPLES + "['unexpected-failure'].value.status")
                        .value(500))
                .andExpect(jsonPath(EXAMPLES + ".*.value.instance").value(everyItem(is("/api/v1/calculations"))));
    }

    @Test
    void documentsProblemSchemaWithOnlyTheFieldsResponsesContain() throws Exception {
        String docs = apiDocs()
                .andExpect(jsonPath("$.components.schemas.ProblemDetail").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Map<String, Object> properties = JsonPath.read(docs, "$.components.schemas.Problem.properties");

        assertThat(properties.keySet()).containsExactlyInAnyOrder("type", "title", "status", "detail", "instance");
    }

    @Test
    void documentsUnknownOperationExampleWithEverySupportedName() throws Exception {
        apiDocs()
                .andExpect(jsonPath(EXAMPLES + "['unknown-operation'].value.detail")
                        .value(containsString(Operation.supportedNames())));
    }

    @Test
    void documentedExamplesMatchRealResponses() throws Exception {
        String docs = apiDocs().andReturn().getResponse().getContentAsString();
        assertMatchesExample(docs, "invalid-request", "{\"operation\":");
        assertMatchesExample(docs, "invalid-request-missing-field", "{\"operation\":\"ADD\",\"a\":1}");
        assertMatchesExample(docs, "operand-out-of-range", "{\"operation\":\"ADD\",\"a\":\"1E+1001\",\"b\":1}");
        assertMatchesExample(docs, "division-by-zero", "{\"operation\":\"DIVIDE\",\"a\":1,\"b\":0}");
        assertMatchesExample(docs, "unknown-operation", "{\"operation\":\"NOPE\",\"a\":1,\"b\":2}");
    }

    private void assertMatchesExample(String docs, String example, String body) throws Exception {
        Map<String, Object> documented = JsonPath.read(docs, EXAMPLES + "['" + example + "'].value");
        String real = mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(JsonPath.<Map<String, Object>>read(real, "$")).as(example).isEqualTo(documented);
    }
}
