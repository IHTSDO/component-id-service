package org.snomed.cis.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSwaggerEndpointsAreAccessibleOrHandled() throws Exception {
        String[] endpoints = {
                "/v3/api-docs",
                "/swagger-ui/index.html",
                "/swagger-ui/swagger-ui.css",
                "/swagger-ui/swagger-ui-bundle.js",
                "/swagger-ui/swagger-ui-standalone-preset.js"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertThat(status)
                                .withFailMessage("Endpoint %s returned unexpected status %d", endpoint, status)
                                .isIn(200, 301, 302, 404); // acceptable
                    });
        }
    }
    @Test
    void swaggerUiEndpoint_shouldReturn200OrRedirect() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(200, 302);
                });
    }

    @Test
    void securedEndpoint_shouldReturnUnauthorizedOrRedirect() throws Exception {
        mockMvc.perform(get("/api/secure-endpoint"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(401, 302);
                });
    }

    @Test
    void apiDocs_shouldReturnJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assertThat(contentType).contains("application/json");
                });
    }

    @Nested
    @SpringBootTest(properties = "springdoc.api-docs.enabled=false")
    class SwaggerDisabledTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void swaggerApiDocs_shouldReturn404_orHandled500_whenDisabled() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertThat(status)
                                .withFailMessage("Expected 404 or 500 for /v3/api-docs but was %d", status)
                                .isIn(404, 500);
                    });
        }
    }

    @Test
    void apiDocs_shouldContainCustomTitle() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"title\":\"SNOMED CT CIS API\"")
                );
    }


}
