package org.snomed.cis.config;

import org.snomed.cis.security.TokenAuthenticationFilter;
import org.snomed.cis.security.TokenAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private TokenAuthenticationProvider tokenAuthenticationProvider;

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            // Other public endpoints
            "/ui/**", //ViewController
            "/admin/**", //UI resource
            "/info/**", //UI resource
            "/login",
            "/loginUI",
            "/sct/check/**",
            "/backendJobServices",
            "/version",
            "/testService"
    };


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(AUTH_WHITELIST);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new TokenAuthenticationFilter(authenticationManager()),
                        AnonymousAuthenticationFilter.class)
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable());
        return httpSecurity.build();

    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(tokenAuthenticationProvider));
    }
}