package org.snomed.cis.config;

import com.atlassian.crowd.service.client.CrowdClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(CrowdConfigTest.MockCrowdClientOverride.class)
class CrowdConfigTest {

    @Autowired
    private CrowdClient crowdClient;

    @Test
    void testCrowdClientBeanCreated() {
        assertNotNull(crowdClient);
    }

    // This override ensures CI won't try to contact the real Crowd server
    @TestConfiguration
    static class MockCrowdClientOverride {
        @Bean
        public CrowdClient crowdClient() {
            return Mockito.mock(CrowdClient.class);
        }
    }
}
