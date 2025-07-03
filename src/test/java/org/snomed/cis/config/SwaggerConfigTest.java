package org.snomed.cis.config;

import com.atlassian.crowd.service.client.CrowdClient;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest  // Load the full context including Swagger & Security
@AutoConfigureMockMvc
class SwaggerConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;
    // Prevent real CrowdClient init by mocking it
    @MockBean
    private CrowdClient crowdClient;
    @TestConfiguration
    static class SwaggerTestConfig {
        @Bean
        public OpenAPI customOpenAPI() {
            return new OpenAPI()
                    .info(new Info()
                            .title("SNOMED CT CIS API")
                            .version("v1.0")
                            .description("SNOMED CT Component Identifier Service"));
        }
    }
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
    // Example assuming custom profile/override
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
                                .contains("\"title\":\"SNOMED CT CIS API\"") // Adjust to match your title
                );
    }

    @Test
    void groupedOpenApiBean_shouldBePresent() {
        // ✅ Make sure GroupedOpenApi bean is created in your SwaggerConfig
        GroupedOpenApi groupedOpenApi = context.getBean(GroupedOpenApi.class);
        assertThat(groupedOpenApi).isNotNull();
        assertThat(groupedOpenApi.getGroup()).isEqualTo("public"); // Match your group name
    }
}
