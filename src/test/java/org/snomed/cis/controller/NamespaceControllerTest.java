package org.snomed.cis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(NamespaceController.class)
class NamespaceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NamespaceService namespaceService;

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void testGetNamespacesForUser_shouldReturnList() throws Exception {
        int username = 12345;
        String token = "dummy-token";

        Namespace ns1 = new Namespace();
        ns1.setNamespace(100001);
        ns1.setOrganizationName("Org A");
        ns1.setOrganizationAndContactDetails("Contact A");
        ns1.setDateIssued(LocalDateTime.of(2024, 1, 1, 0, 0));
        ns1.setEmail("orga@example.com");
        ns1.setNotes("note1");
        ns1.setIdPregenerate("Y");

        Namespace ns2 = new Namespace();
        ns2.setNamespace(100002);
        ns2.setOrganizationName("Org B");
        ns2.setOrganizationAndContactDetails("Contact B");
        ns2.setDateIssued(LocalDateTime.of(2024, 1, 2, 0, 0));
        ns2.setEmail("orgb@example.com");
        ns2.setNotes("note2");
        ns2.setIdPregenerate("N");

        when(namespaceService.getNamespacesForUser(String.valueOf(username)))
                .thenReturn(List.of(ns1, ns2));

        mockMvc.perform(get("/users/{username}/namespaces/", username)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].namespace").value(100001))
                .andExpect(jsonPath("$[1].organizationName").value("Org B"));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespacesForUser_missingToken_shouldReturnBadRequest() throws Exception {
        int username = 12345;

        mockMvc.perform(get("/users/{username}/namespaces/", username)
                                   .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespacesForUser_noNamespaces_shouldReturnEmptyList() throws Exception {
        int username = 99999;
        String token = "dummy-token";

        when(namespaceService.getNamespacesForUser(String.valueOf(username))).thenReturn(List.of());

        mockMvc.perform(get("/users/{username}/namespaces/", username)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespacesForUser_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        int username = 12345;
        String token = "dummy-token";

        when(namespaceService.getNamespacesForUser(String.valueOf(username)))
                .thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

        mockMvc.perform(get("/users/{username}/namespaces/", username)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("Internal error"));
    }
    @Test
    void testGetNamespacesForUser_unauthenticated_shouldReturnUnauthorized() throws Exception {
        int username = 12345;
        String token = "dummy-token";

        mockMvc.perform(get("/users/{username}/namespaces/", username)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespacesForUser_invalidUsernameFormat_shouldReturnEmptyList() throws Exception {
        String invalidUsername = "abc";
        String token = "dummy-token";

        when(namespaceService.getNamespacesForUser(invalidUsername)).thenReturn(List.of());

        mockMvc.perform(get("/users/{username}/namespaces/", invalidUsername)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void testGetNamespaces_shouldReturnList() throws Exception {
        String token = "dummy-token";

        when(namespaceService.getNamespaces()).thenReturn(List.of());

        mockMvc.perform(get("/sct/namespaces")
                        .param("token", token)
                        .with(user("testuser").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespaces_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        String token = "dummy-token";

        when(namespaceService.getNamespaces())
                .thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

        mockMvc.perform(get("/sct/namespaces")
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("Internal error"));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespaces_noToken_shouldReturnList() throws Exception {
        when(namespaceService.getNamespaces()).thenReturn(List.of());

        mockMvc.perform(get("/sct/namespaces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespaces_emptyList_shouldReturnEmptyJsonArray() throws Exception {
        when(namespaceService.getNamespaces()).thenReturn(List.of());

        mockMvc.perform(get("/sct/namespaces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespaces_validList_shouldReturnJson() throws Exception {
        NamespaceDto ns1 = new NamespaceDto(); NamespaceDto ns2 = new NamespaceDto();
        when(namespaceService.getNamespaces()).thenReturn(List.of(ns1, ns2));

        mockMvc.perform(get("/sct/namespaces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    @Test
    void testGetNamespaces_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/sct/namespaces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testUpdateNamespace_missingBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Expect 400
    }

    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespaces_withInvalidTokenParam_shouldReturnOk() throws Exception {
        when(namespaceService.getNamespaces()).thenReturn(List.of());

        mockMvc.perform(get("/sct/namespaces")
                        .param("token", "invalid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_validNamespaceId_shouldReturnNamespaceDto() throws Exception {
        String token = "dummy-token";
        String namespaceId = "100001";Partitions partition1 = new Partitions();
        partition1.setNamespace(Integer.valueOf(namespaceId)); NamespaceDto nsDto = new NamespaceDto();
        nsDto.setNamespace(Integer.valueOf(namespaceId));
        nsDto.setOrganizationName("Org A");
        nsDto.setOrganizationAndContactDetails("Contact A");
        nsDto.setDateIssued(String.valueOf(LocalDateTime.of(2024, 1, 1, 0, 0)));
        nsDto.setNotes("note1");
        nsDto.setIdPregenerate("Y");
        nsDto.setPartitions(List.of(partition1));

        when(namespaceService.getNamespace(namespaceId)).thenReturn(nsDto);

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namespace").value(namespaceId))
                .andExpect(jsonPath("$.organizationName").value("Org A"))
                .andExpect(jsonPath("$.partitions").isArray())
                .andExpect(jsonPath("$.partitions[0].namespace").value(namespaceId));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_namespaceNotFound_shouldReturnNotFound() throws Exception {
        String token = "dummy-token";
        String namespaceId = "999999";

        when(namespaceService.getNamespace(namespaceId)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "Namespace not found"));

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Namespace not found"));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_missingToken_shouldReturnBadRequest() throws Exception {
        String namespaceId = "100001";

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", namespaceId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_invalidNamespaceIdFormat_shouldReturnBadRequest() throws Exception {
        String token = "dummy-token";
        String invalidNamespaceId = "abc123";when(namespaceService.getNamespace(invalidNamespaceId)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid namespace id"));

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", invalidNamespaceId)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid namespace id"));
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String token = "dummy-token";
        String namespaceId = "100001";

        when(namespaceService.getNamespace(namespaceId)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Unexpected error")));
    }
    @Test
    void testGetNamespace_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String token = "dummy-token";
        String namespaceId = "100001";

        mockMvc.perform(get("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    @WithMockUser(roles = {"USER"})
    @Test
    void testGetNamespace_invalidHttpMethod_shouldReturnMethodNotAllowed() throws Exception {
        String token = "dummy-token";

        mockMvc.perform(post("/sct/namespaces/100001")
                        .param("token", token)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    void testCreateNamespace_shouldReturnSuccess() throws Exception {
        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder()
                .name("testuser")
                .email("test@example.com")
                .roles(List.of("ROLE_USER")).build();
        NamespaceDto requestDto = new NamespaceDto();
        requestDto.setNamespace(12345);
        requestDto.setOrganizationName("OrgName Example");
        requestDto.setOrganizationAndContactDetails("Contact details example");
        requestDto.setDateIssued(String.valueOf(LocalDate.of(2024, 1, 1)));
        requestDto.setNotes("Sample notes");
        requestDto.setIdPregenerate("Y"); when(namespaceService.createNamespace(any(), any())).thenReturn("Namespace created successfully");
Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken(
                "dummy-token", "testuser", mockDto,
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Namespace created successfully"));
    }
    @Test
    void testCreateNamespace_success_shouldReturnOk() throws Exception {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(12345);
        namespaceDto.setOrganizationName("OrgName Example");
        namespaceDto.setOrganizationAndContactDetails("Contact details example");
        namespaceDto.setDateIssued(String.valueOf(LocalDate.parse("2024-01-01")));
        namespaceDto.setEmail("org@example.com");
        namespaceDto.setNotes("Sample notes");
        namespaceDto.setIdPregenerate("Y");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .name("testuser")
                .firstName("Test")
                .lastName("User")
                .displayName("Test User")
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.createNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenReturn("Namespace Created Successfully");

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(namespaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Namespace Created Successfully"));
    }
    @Test
    void testCreateNamespace_missingFields_shouldReturnBadRequest() throws Exception {
        NamespaceDto namespaceDto = new NamespaceDto(); // empty DTO

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        // Simulate service rejecting invalid input by throwing CisException with 400
        when(namespaceService.createNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Missing required fields"));

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(namespaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));
    }

    @Test
    void testCreateNamespace_unauthorizedUser_shouldReturnForbidden() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("Unauthorized Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("unauth@example.com")
                .roles(List.of("ROLE_VIEWER"))
                .build();

        Token authToken = new Token("dummy-token", "unauth", true, List.of(), authDto);
 when(namespaceService.createNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new CisException(HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void testCreateNamespace_duplicate_shouldReturnConflict() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("Duplicate Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.createNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new CisException(HttpStatus.CONFLICT,"Namespace already exists"));

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
    @Test
    void testCreateNamespace_internalError_shouldReturnServerError() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.createNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void testCreateNamespace_missingCsrf_shouldReturnForbidden() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("CSRF Test Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(post("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))) // CSRF missing
                .andExpect(status().isForbidden());
    }
    @Test
    void testUpdateNamespace_success_shouldReturnOk() throws Exception {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(12345);
        namespaceDto.setOrganizationName("Updated Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.updateNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenReturn("Namespace Updated Successfully");

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(namespaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Namespace Updated Successfully"));
    }
    @Test
    void testUpdateNamespace_unauthorizedUser_shouldReturnForbidden() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("Unauthorized Org");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("unauth@example.com")
                .roles(List.of("ROLE_VIEWER"))
                .build();

        Token authToken = new Token("dummy-token", "unauth", true, List.of(), authDto);

        when(namespaceService.updateNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void testUpdateNamespace_invalidInput_shouldReturnBadRequest() throws Exception {
        NamespaceDto invalidDto = new NamespaceDto();

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.updateNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid Namespace Data"));

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Invalid Namespace Data"));
    }
    @Test
    void testUpdateNamespace_missingToken_shouldReturnUnauthorized() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("No Token Org");

        mockMvc.perform(put("/sct/namespaces")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void testUpdateNamespace_missingCsrf_shouldReturnForbidden() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("CSRF Test");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken)))
                .andExpect(status().isForbidden());
    }
    @Test
    void testUpdateNamespace_malformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{namespace:12345, organizationName:\"Invalid JSON\"";

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(malformedJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken)) // <-- Fix
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void testUpdateNamespace_withDeleteMethod_shouldReturnMethodNotAllowed() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(delete("/sct/namespaces")
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Request method 'DELETE' is not supported"));
    }


    @Test
    void testUpdateNamespace_internalServerError_shouldReturn500() throws Exception {
        NamespaceDto dto = new NamespaceDto();
        dto.setNamespace(12345);
        dto.setOrganizationName("Org With Error");

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.updateNamespace(eq(authDto), any(NamespaceDto.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/sct/namespaces")
                        .param("token", "dummy-token")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Unexpected error"));
    }
    @Test
    void testDeleteNamespace_success_shouldReturnOk() throws Exception {
        String namespaceId = "12345";

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.deleteNamespace((authDto), (namespaceId)))
                .thenReturn("Namespace Deleted Successfully");

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Namespace Deleted Successfully"));
    }
    @Test
    void testDeleteNamespace_unauthorizedRole_shouldReturnForbidden() throws Exception {
        String namespaceId = "12345";

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("unauth@example.com")
                .roles(List.of("ROLE_VIEWER"))
                .build();

        Token authToken = new Token("dummy-token", "unauth", true, List.of(), authDto);

        when(namespaceService.deleteNamespace((authDto), (namespaceId)))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }
    @Test
    void testDeleteNamespace_notFound_shouldReturnNotFound() throws Exception {
        String namespaceId = "99999";

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.deleteNamespace((authDto), (namespaceId)))
                .thenThrow(new CisException(HttpStatus.NOT_FOUND, "Namespace not found"));

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", namespaceId)
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Namespace not found"));
    }
    @Test
    void testDeleteNamespace_missingToken_shouldReturnBadRequest() throws Exception {
        String namespaceId = "12345";

        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", namespaceId)
                        // Missing .param("token", ...)
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
    @Test
    void testDeleteNamespace_noAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", "12345")
                        .param("token", "dummy-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void testDeleteNamespace_missingCsrf_shouldReturnForbidden() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", "12345")
                        .param("token", "dummy-token")
                        .with(authentication(authToken))) // No CSRF
                .andExpect(status().isForbidden());
    }
    @Test
    void testDeleteNamespace_internalServerError_shouldReturn500() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.deleteNamespace((authDto), ("12345")))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", "12345")
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unexpected error")));
    }
    @Test
    void testDeleteNamespace_conflict_shouldReturn409() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.deleteNamespace((authDto), ("12345")))
                .thenThrow(new CisException(HttpStatus.CONFLICT, "Namespace already deleted or locked"));

        mockMvc.perform(delete("/sct/namespaces/{namespaceId}", "12345")
                        .param("token", "dummy-token")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("Namespace already deleted or locked"));
    }
    @Test
    void testUpdatePartitionSequence_success_shouldReturnOk() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.updatePartitionSequence((authDto), ("12345"), ("partitionA"), ("100")))
                .thenReturn("Partition sequence updated successfully");

        mockMvc.perform(put("/sct/namespaces/12345/partition/partitionA")
                        .param("token", "dummy-token")
                        .param("value", "100")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Partition sequence updated successfully"));
    }
    @Test
    void testUpdatePartitionSequence_unauthorized_shouldReturnForbidden() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("unauth@example.com")
                .roles(List.of("ROLE_VIEWER"))
                .build();

        Token authToken = new Token("dummy-token", "unauth", true, List.of(), authDto);

        when(namespaceService.updatePartitionSequence(eq(authDto), any(), any(), any()))
                .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(put("/sct/namespaces/12345/partition/partitionA")
                        .param("token", "dummy-token")
                        .param("value", "100")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }
    @Test
    void testUpdatePartitionSequence_missingValueParam_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        mockMvc.perform(put("/sct/namespaces/12345/partition/partitionA")
                        .param("token", "dummy-token")
                        // value param is missing
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.statusCode").value(400));
    }
    @Test
    void testUpdatePartitionSequence_conflict_shouldReturn409() throws Exception {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .build();

        Token authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);

        when(namespaceService.updatePartitionSequence(eq(authDto), any(), any(), any()))
                .thenThrow(new CisException(HttpStatus.CONFLICT, "Sequence already exists"));

        mockMvc.perform(put("/sct/namespaces/12345/partition/partitionA")
                        .param("token", "dummy-token")
                        .param("value", "100")
                        .with(authentication(authToken))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("Sequence already exists"));
    }
    @Test
    void testUpdatePartitionSequence_noAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/sct/namespaces/12345/partition/partitionA")
                        .param("token", "dummy-token")
                        .param("value", "100")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}
