package org.snomed.cis.config;

import com.atlassian.crowd.service.client.CrowdClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Integration test to verify that the CrowdClient bean is correctly created
 * and injected by the Spring context using the provided test properties.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Override Crowd configuration properties for the test context
        "crowd.url=http://localhost:8095/crowd/",
        "crowd.application=cis-app",
        "crowd.password=cis-secret"
})
class CrowdConfigTest {

    // Inject the CrowdClient bean from the Spring context
    @Autowired
    private CrowdClient crowdClient;

    /**
     * Test to ensure that the CrowdClient bean is not null,
     * indicating successful creation and injection.
     */
    @Test
    void testCrowdClientBeanCreated() {
        assertNotNull(crowdClient);
    }
}