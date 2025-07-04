package org.snomed.cis.config;

import org.junit.jupiter.api.Test;
import org.snomed.cis.security.TokenAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void testAuthenticationManager_shouldReturnProviderManagerWithCustomProvider() {
        // Arrange
        TokenAuthenticationProvider mockProvider = mock(TokenAuthenticationProvider.class);
        SecurityConfig securityConfig = new SecurityConfig(mockProvider);

        // Act
        AuthenticationManager manager = securityConfig.authenticationManager();

        // Assert
        assertNotNull(manager);
        assertTrue(manager instanceof ProviderManager);

        List<?> providers = ((ProviderManager) manager).getProviders();
        assertEquals(1, providers.size());
        assertSame(mockProvider, providers.get(0));
    }

    @Test
    void testWebSecurityCustomizer_shouldNotBeNull() {
        TokenAuthenticationProvider mockProvider = mock(TokenAuthenticationProvider.class);
        SecurityConfig config = new SecurityConfig(mockProvider);

        assertNotNull(config.webSecurityCustomizer());
    }

}
