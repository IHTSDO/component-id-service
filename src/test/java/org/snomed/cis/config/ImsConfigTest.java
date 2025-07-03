package org.snomed.cis.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ImsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ImsConfig.class)
            .withPropertyValues(
                    "ims.cookiename=ims-token",
                    "ims.urls.base=http://localhost:8080/ims",
                    "ims.urls.login=/login",
                    "ims.urls.logout=/logout",
                    "ims.urls.authenticate=/auth"
            );

    @Test
    void testImsConfigValues() {
        contextRunner.run(context -> {
            ImsConfig config = context.getBean(ImsConfig.class);
            assertThat(config).isNotNull();
            assertThat(config.getCookieName()).isEqualTo("ims-token");
            assertThat(config.getBaseUrl()).isEqualTo("http://localhost:8080/ims");
            assertThat(config.getLoginUrl()).isEqualTo("/login");
            assertThat(config.getLogoutUrl()).isEqualTo("/logout");
            assertThat(config.getAuthenticateUrl()).isEqualTo("/auth");
        });
    }

}
