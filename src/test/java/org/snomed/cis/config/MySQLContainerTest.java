package org.snomed.cis.config;

import com.atlassian.crowd.service.client.CrowdClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = MySQLContainerTest.Initializer.class)
class MySQLContainerTest {

    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26").withDatabaseName("cis").withUsername("cis").withPassword("testpass");

    static {
        mysqlContainer.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("spring.datasource.url=" + mysqlContainer.getJdbcUrl(), "spring.datasource.username=" + mysqlContainer.getUsername(), "spring.datasource.password=" + mysqlContainer.getPassword(), "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver", "spring.jpa.hibernate.ddl-auto=update",

                    // 👇 Required to avoid CrowdConfig failure
                    "crowd.url=http://localhost:8095", "crowd.application=test-app", "crowd.password=test-pass").applyTo(context.getEnvironment());
        }
    }

    @MockBean
    private CrowdClient crowdClient;

    @Test
    void testContainerIsRunning() {
        assertTrue(mysqlContainer.isRunning());
    }
}
