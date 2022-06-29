package org.snomed.cis.config;

import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.client.CrowdClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrowdConfig {

    @Value("${crowd.url}")
    private String url;

    @Value("${crowd.application}")
    private String application;

    @Value("${crowd.password}")
    private String password;

    @Bean
    public CrowdClient init() {
        return new RestCrowdClientFactory().newInstance(url, application, password);
    }
}
