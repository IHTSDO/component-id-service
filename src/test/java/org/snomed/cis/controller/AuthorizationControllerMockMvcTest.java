package org.snomed.cis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.AuthorizationService;
import org.snomed.cis.service.NamespaceService;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorizationController.class)
class AuthorizationControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private Token mockToken;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private NamespaceService namespaceService;

    @MockBean
    private SchemeService schemeService;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(mockToken);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetUserGroups_success() throws Exception {
        List<String> mockGroups = Arrays.asList("group1", "group2");
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin((Token) authToken);
        Mockito.when(authorizationService.getUserGroups("dummy-token","john")).thenReturn(mockGroups);

        mockMvc.perform(get("/users/john/groups")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("group1"))
                .andExpect(jsonPath("$[1]").value("group2"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetUserGroups_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/john/groups")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetUserGroups_userNotFound_shouldReturnInternalServerError() throws Exception {
        CisException exception = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");

        when(authorizationService.getUserGroups("dummy-token","user")).thenThrow(exception);

        mockMvc.perform(get("/users/invalidUser/groups").param("token", "dummy-token")).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetUserGroups_emptyList_shouldReturnOk() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Token authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin(authToken);
        Mockito.when(authorizationService.getUserGroups("dummy-token","john")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/john/groups")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void testGetGroupUsers_validToken_shouldReturnUsers() throws Exception {
        List<String> mockUsers = Arrays.asList("user1", "user2");

        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Token authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin(authToken);
        Mockito.when(authorizationService.getGroupUsers("dummy-token","devGroup")).thenReturn(mockUsers);

        mockMvc.perform(get("/groups/devGroup/users")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("user1"))
                .andExpect(jsonPath("$[1]").value("user2"));
    }


    @Test
    void testGetGroupUsers_invalidGroup_shouldThrowCisException() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Token authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin(authToken);
        Mockito.when(authorizationService.getGroupUsers("dummy-token", "invalidGroup"))
                .thenThrow(new CisException(HttpStatus.NOT_FOUND, "Group not found"));

        mockMvc.perform(get("/groups/invalidGroup/users")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Group not found"));
    }



    @WithAnonymousUser
    @Test
    void testGetGroupUsers_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/groups/devGroup/users").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetGroupUsers_internalServerError_shouldReturn500() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Token authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin(authToken);

        Mockito.when(authorizationService.getGroupUsers("dummy-token","devGroup"))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/groups/devGroup/users")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Something went wrong"));
    }

    @Test
    void testGetNamespacePermissions_validRequest_shouldReturnPermissions() throws Exception {
        List<PermissionsNamespace> mockPermissions = Arrays.asList(
                new PermissionsNamespace(123456, "admin", "read"),
                new PermissionsNamespace(123456, "admin", "write")
        );

        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Token authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.doNothing().when(authorizationService).validateAdmin(authToken);
        Mockito.when(namespaceService.isAbleToEdit(123456, mockDto)).thenReturn(true);
        Mockito.when(namespaceService.getNamespacePermissions("123456"))
                .thenReturn(mockPermissions);

        mockMvc.perform(get("/sct/namespaces/123456/permissions")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("read"))
                .andExpect(jsonPath("$[0].namespace").value(123456))
                .andExpect(jsonPath("$[1].role").value("write"))
                .andExpect(jsonPath("$[1].namespace").value(123456));
    }

    @Test
    void testGetNamespacePermissions_invalidNamespace_shouldReturnNotFound() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.isAbleToEdit(Mockito.anyInt(), Mockito.eq(mockDto))).thenReturn(true);
        Mockito.when(namespaceService.getNamespacePermissions("000"))
                .thenThrow(new CisException(HttpStatus.NOT_FOUND, "Namespace not found"));

        mockMvc.perform(get("/sct/namespaces/000/permissions")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Namespace not found"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetNamespacePermissions_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/sct/namespaces/123456/permissions")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @WithAnonymousUser
    @Test
    void testGetNamespacePermissions_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/sct/namespaces/123456/permissions").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetNamespacePermissions_internalError_shouldReturn500() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_component-identifier-service-admin"));
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.isAbleToEdit(123456, mockDto)).thenReturn(true);
        Mockito.when(namespaceService.getNamespacePermissions("123456"))
                .thenThrow(new RuntimeException("Something failed"));

        mockMvc.perform(get("/sct/namespaces/123456/permissions")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Something failed"));
    }


    @Test
    void testDeleteNamespacePermissions_valid_shouldReturnOk() throws Exception {

        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));


        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.deleteNamespacePermissionsOfUser("123456", "admin", mockDto)).thenReturn("Deleted");

        mockMvc.perform(delete("/sct/namespaces/123456/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string("Deleted"));
    }


    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testDeleteNamespacePermissions_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/sct/namespaces/123456/permissions/admin").with(csrf())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @Test
    void testDeleteNamespacePermissions_serviceThrows_shouldReturnInternalServerError() throws Exception {

        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.deleteNamespacePermissionsOfUser("123456", "admin", mockDto)).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(delete("/sct/namespaces/123456/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Simulated failure"));
    }

    @WithAnonymousUser
    @Test
    void testDeleteNamespacePermissions_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/sct/namespaces/123456/permissions/admin").param("token", "dummy-token").with(csrf())).andExpect(status().isUnauthorized());
    }


    static class TestToken extends Token {

        public TestToken(String token, String username, AuthenticateResponseDto dto, List<GrantedAuthority> authorities) {
            super(token, username, true, authorities, dto);
        }

        @Override
        public String getName() {
            return super.getUserName();
        }
    }

    @Test
    void testCreateNamespacePermissions_valid_shouldReturnOk() throws Exception {

        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.createNamespacePermissionsOfUser("123456", "admin", "ADMIN", mockDto)).thenReturn("Permission created");

        mockMvc.perform(post("/sct/namespaces/123456/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string("Permission created"));
    }

    @Test
    void testCreateNamespacePermissions_missingToken_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));


        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        mockMvc.perform(post("/sct/namespaces/123456/permissions/admin").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @Test
    void testCreateNamespacePermissions_serviceThrows_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(namespaceService.createNamespacePermissionsOfUser("123456", "admin", "ADMIN", mockDto)).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(post("/sct/namespaces/123456/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Simulated failure"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetPermissionsForScheme_success() throws Exception {
        List<PermissionsScheme> mockPermissions = List.of(new PermissionsScheme("SCHEME123", "admin", "read"), new PermissionsScheme("SCHEME123", "admin", "write"));

        Mockito.when(schemeService.getPermissionsForScheme("SCHEME123")).thenReturn(mockPermissions);

        mockMvc.perform(get("/schemes/SCHEME123/permissions").param("token", "dummy-token")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].scheme").value("SCHEME123")).andExpect(jsonPath("$[0].role").value("read")).andExpect(jsonPath("$[1].role").value("write"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetPermissionsForScheme_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/schemes/SCHEME123/permissions")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetPermissionsForScheme_invalidScheme_shouldReturnInternalServerError() throws Exception {
        Mockito.when(schemeService.getPermissionsForScheme("INVALID")).thenThrow(new RuntimeException("Scheme not found"));

        mockMvc.perform(get("/schemes/INVALID/permissions").param("token", "dummy-token")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Scheme not found"));
    }

    @WithMockUser(username = "admin", roles = {"USER"})
    @Test
    void testGetPermissionsForScheme_serviceThrows_shouldReturnInternalServerError() throws Exception {
        Mockito.when(schemeService.getPermissionsForScheme("CRASH")).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(get("/schemes/CRASH/permissions").param("token", "dummy-token")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Simulated failure"));
    }


    @Test
    void testDeleteSchemePermissions_validRequest_shouldReturnOk() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(schemeService.deleteSchemePermissions("SCHEME123", "admin", mockDto)).thenReturn("Deleted");

        mockMvc.perform(delete("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string("Deleted"));
    }

    @Test
    void testDeleteSchemePermissions_serviceThrows_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.when(schemeService.deleteSchemePermissions("SCHEME123", "admin", mockDto)).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(delete("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Simulated failure"));
    }

    @Test
    void testDeleteSchemePermissions_notFound_shouldReturn404() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.when(schemeService.deleteSchemePermissions("INVALID", "admin", mockDto)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "Scheme not found"));

        mockMvc.perform(delete("/schemes/INVALID/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isNotFound()).andExpect(jsonPath("$.statusCode").value(404)).andExpect(jsonPath("$.message").value("Scheme not found"));
    }

    @Test
    void testDeleteSchemePermissions_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").with(csrf())) // no .with(authentication(...))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteSchemePermissions_accessDenied_shouldReturnForbidden() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doThrow(new AccessDeniedException("Access is denied")).when(schemeService).deleteSchemePermissions("SCHEME123", "admin", mockDto);

        mockMvc.perform(delete("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isForbidden()).andExpect(jsonPath("$.statusCode").value(403)).andExpect(jsonPath("$.message").value("Access is denied"));
    }


    @Test
    void testCreateSchemePermissions_validRequest_shouldReturnOk() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, authorities);

        Mockito.when(schemeService.createSchemePermissions("SCHEME123", "admin", "ADMIN", mockDto)).thenReturn("Permission created");

        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string("Permission created"));
    }

    @Test
    void testCreateSchemePermissions_missingToken_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
    }

    @Test
    void testCreateSchemePermissions_missingRole_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").with(csrf()).with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Required request parameter 'role' for method parameter type String is not present"));
    }

    @Test
    void testCreateSchemePermissions_serviceThrows_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.when(schemeService.createSchemePermissions("SCHEME123", "admin", "ADMIN", mockDto)).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Simulated failure"));
    }

    @Test
    void testCreateSchemePermissions_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf())) // no auth token
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateSchemePermissions_accessDenied_shouldReturnForbidden() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.when(schemeService.createSchemePermissions("SCHEME123", "admin", "ADMIN", mockDto)).thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(post("/schemes/SCHEME123/permissions/admin").param("token", "dummy-token").param("role", "ADMIN").with(csrf()).with(authentication(authToken))).andExpect(status().isForbidden()).andExpect(jsonPath("$.statusCode").value(403)).andExpect(jsonPath("$.message").value("Access is denied"));
    }


}
