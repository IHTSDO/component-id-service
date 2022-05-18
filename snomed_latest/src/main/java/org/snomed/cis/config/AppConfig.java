package org.snomed.cis.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;
import org.snomed.cis.pojo.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    @Bean
    public Config getConfig() throws IOException {
        InputStream configFileInputStream = new ClassPathResource("json/config.json").getInputStream();
        String configFileContents = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(configFileInputStream))) {
            configFileContents = reader.lines()
                    .collect(Collectors.joining("\n"));
        }
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        StringSubstitutor stringSubstitutor = new StringSubstitutor();
        return objectMapper.readValue(stringSubstitutor.replace(configFileContents), Config.class);
    }

}
