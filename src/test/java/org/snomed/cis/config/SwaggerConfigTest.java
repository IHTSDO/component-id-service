package org.snomed.cis.config;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    @Test
    void testPublicApiBeanShouldNotBeNull() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        GroupedOpenApi groupedOpenApi = swaggerConfig.publicApi();

        assertNotNull(groupedOpenApi, "GroupedOpenApi bean should not be null");
        assertEquals("public", groupedOpenApi.getGroup(), "API group name should be 'public'");
    }
}
