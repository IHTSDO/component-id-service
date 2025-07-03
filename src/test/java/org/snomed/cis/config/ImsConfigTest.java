package org.snomed.cis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test to verify that the ImsConfig bean is correctly created
 * and injected by the Spring context using the provided test properties.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Override IMS configuration properties for the test context
        "ims.cookiename=ims-token",
        "ims.urls.base=http://localhost:8080/ims",
        "ims.urls.login=/login",
        "ims.urls.logout=/logout",
        "ims.urls.authenticate=/auth"
})
class ImsConfigTest {

    // Inject the ImsConfig bean from the Spring context
    @Autowired
    private ImsConfig imsConfig;

    /**
     * Test to ensure that the ImsConfig bean is not null and
     * its properties are set to the expected values.
     */
    @Test
    void testImsConfigValues() {
        assertNotNull(imsConfig); // Check that the bean is injected
        assertEquals("ims-token", imsConfig.getCookieName()); // Validate cookie name
        assertEquals("http://localhost:8080/ims", imsConfig.getBaseUrl()); // Validate base URL
        assertEquals("/login", imsConfig.getLoginUrl()); // Validate login URL
        assertEquals("/logout", imsConfig.getLogoutUrl()); // Validate logout URL
        assertEquals("/auth", imsConfig.getAuthenticateUrl()); // Validate authenticate URL
    }
}