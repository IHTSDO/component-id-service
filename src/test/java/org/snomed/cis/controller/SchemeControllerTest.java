package org.snomed.cis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.Scheme;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchemeController.class)
class SchemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemeService schemeService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticateResponseDto authDto;
    private Token authToken;

    @BeforeEach
    void setup() {
        authDto = AuthenticateResponseDto.builder().name("testuser").roles(List.of("ROLE_USER")).build();

        authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);
    }

    @Test
    void testGetSchemesForUser_success_shouldReturnSchemes() throws Exception {
        Scheme scheme1 = new Scheme();
        scheme1.setName(SchemeName.SNOMEDID.name());
        scheme1.setDescription("SNOMED Description");

        Scheme scheme2 = new Scheme();
        scheme2.setName(SchemeName.CTV3ID.name());
        scheme2.setDescription("CTV3 Description");

        List<Scheme> schemes = List.of(scheme1, scheme2);

        when(schemeService.getSchemesForUser((authDto), ("testuser"))).thenReturn(schemes);

        mockMvc.perform(get("/users/testuser/schemes/").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$[0].name").value("SNOMEDID")).andExpect(jsonPath("$[0].description").value("SNOMED Description")).andExpect(jsonPath("$[1].name").value("CTV3ID")).andExpect(jsonPath("$[1].description").value("CTV3 Description"));
    }

    @Test
    void testGetSchemesForUser_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/testuser/schemes/").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSchemesForUser_noSchemes_shouldReturnEmptyList() throws Exception {
        when(schemeService.getSchemesForUser((authDto), ("testuser"))).thenReturn(List.of());

        mockMvc.perform(get("/users/testuser/schemes/").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(content().json("[]"));
    }

    @Test
    void testGetSchemesForUser_cisException_shouldReturnInternalServerError() throws Exception {
        when(schemeService.getSchemesForUser((authDto), ("testuser"))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/users/testuser/schemes/").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSchemesForUser_usernameNotFound_shouldReturn500() throws Exception {
        when(schemeService.getSchemesForUser((authDto), ("unknownUser"))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/users/unknownUser/schemes/").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSchemes_success_shouldReturnList() throws Exception {
        when(schemeService.getSchemes()).thenReturn(List.of(new SchemeIdBase("SNOMEDID", "SNOMED Desc"), new SchemeIdBase("CTV3ID", "CTV3 Desc")));

        mockMvc.perform(get("/schemes").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$[0].scheme").value("SNOMEDID")).andExpect(jsonPath("$[0].idBase").value("SNOMED Desc")).andExpect(jsonPath("$[1].scheme").value("CTV3ID")).andExpect(jsonPath("$[1].idBase").value("CTV3 Desc"));

    }

    @Test
    void testGetSchemes_emptyList_shouldReturnEmptyArray() throws Exception {
        when(schemeService.getSchemes()).thenReturn(List.of());

        mockMvc.perform(get("/schemes").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSchemes_nullResponse_shouldReturnOkWithNoBody() throws Exception {
        when(schemeService.getSchemes()).thenReturn(null);

        mockMvc.perform(get("/schemes").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    void testGetSchemes_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/schemes").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSchemes_unauthorized_shouldReturn401() throws Exception {
        mockMvc.perform(get("/schemes").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSchemes_serviceThrowsException_shouldReturn500() throws Exception {
        when(schemeService.getSchemes()).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Service failure"));

        mockMvc.perform(get("/schemes").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Service failure"));
    }

    @Test
    void testGetSchemes_partialData_shouldReturnValidResponse() throws Exception {
        when(schemeService.getSchemes()).thenReturn(List.of(new SchemeIdBase("SNOMEDID", null)));

        mockMvc.perform(get("/schemes").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$[0].scheme").value("SNOMEDID")).andExpect(jsonPath("$[0].idBase").doesNotExist());
    }

    @Test
    void testGetScheme_success_shouldReturnScheme() throws Exception {
        String schemeName = "SNOMEDID";
        SchemeIdBase expectedScheme = new SchemeIdBase("SNOMEDID", "SNOMED Desc");

        when(schemeService.getScheme(schemeName)).thenReturn(expectedScheme);

        mockMvc.perform(get("/schemes/{schemeName}", schemeName).param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$.scheme").value("SNOMEDID")).andExpect(jsonPath("$.idBase").value("SNOMED Desc"));
    }

    @Test
    void testGetScheme_notFound_shouldReturn404() throws Exception {
        String schemeName = "INVALID";

        when(schemeService.getScheme(schemeName)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "Scheme not found"));

        mockMvc.perform(get("/schemes/{schemeName}", schemeName).param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isNotFound());
    }

    @Test
    void testGetScheme_nullResponse_shouldReturnOkWithEmptyBody() throws Exception {
        when(schemeService.getScheme("SNOMEDID")).thenReturn(null);

        mockMvc.perform(get("/schemes/SNOMEDID").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    void testGetScheme_missingToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/schemes/{schemeName}", "SNOMEDID")).andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateScheme_success_shouldReturnOk() throws Exception {
        when(schemeService.updateScheme((authDto), (SchemeName.SNOMEDID), ("10001"))).thenReturn("Scheme updated");

        mockMvc.perform(put("/schemes/SNOMEDID").param("schemeSeq", "10001").with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().string("Scheme updated"));
    }

    @Test
    void testUpdateScheme_missingSchemeSeq_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/schemes/SNOMEDID")
                // No schemeSeq param
                .with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateScheme_invalidSchemeName_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/schemes/INVALID_SCHEME").param("schemeSeq", "10001").with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateScheme_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/schemes/SNOMEDID").param("schemeSeq", "10001").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateScheme_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        when(schemeService.updateScheme((authDto), (SchemeName.SNOMEDID), ("10001"))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Update failed"));

        mockMvc.perform(put("/schemes/SNOMEDID").param("schemeSeq", "10001").with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Update failed"));
    }

}
