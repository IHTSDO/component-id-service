package org.snomed.cis.config;

import com.atlassian.crowd.service.client.CrowdClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CrowdConfigTest.MockCrowdClientOverride.class})
class CrowdConfigTest {

    @Autowired
    private CrowdClient crowdClient;

    @Test
    void testCrowdClientBeanCreated() {
        assertNotNull(crowdClient);
    }

    @TestConfiguration
    static class MockCrowdClientOverride {
        @Bean
        public CrowdClient crowdClient() {
            return Mockito.mock(CrowdClient.class);
        }
    }
}
