package org.snomed.cis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkSchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BulkSchemeIdController.class)
class BulkSchemeIdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BulkSchemeIdService bulkSchemeIdService;

    static class TestToken extends Token {
        public TestToken(String token, String username, AuthenticateResponseDto dto) {
            super(token, username, true, List.of(), dto);
        }
    }

    @Test
    void testGetSchemeIds_shouldReturnListOfIds() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeIds = "100001,100002";

        List<SchemeId> mockResponse = List.of(new SchemeId(), new SchemeId());

        when(bulkSchemeIdService.getSchemeIds(mockDto, schemeName, schemeIds)).thenReturn(mockResponse);

        Authentication authToken = new TestToken("dummy-token", "admin", mockDto);

        mockMvc.perform(get("/scheme/SNOMEDID/bulk").param("token", "dummy-token").param("schemeIds", schemeIds).with(authentication(authToken)).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetSchemeIds_shouldReturnEmptyList() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeIds = "123456,654321";

        when(bulkSchemeIdService.getSchemeIds(mockDto, schemeName, schemeIds)).thenReturn(List.of());

        Authentication authToken = new TestToken("dummy-token", "admin", mockDto);

        mockMvc.perform(get("/scheme/SNOMEDID/bulk").param("token", "dummy-token").param("schemeIds", schemeIds).with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSchemeIds_missingSchemeIds_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto);

        mockMvc.perform(get("/scheme/SNOMEDID/bulk").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testGetSchemeIds_invalidEnum_shouldReturnError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto);

        mockMvc.perform(get("/scheme/INVALID/bulk").param("token", "dummy-token").param("schemeIds", "123").with(authentication(authToken))).andExpect(status().is4xxClientError());
    }

    @Test
    void testGetSchemeIds_serviceThrowsCisException_shouldReturnErrorResponse() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new TestToken("dummy-token", "admin", mockDto);

        when(bulkSchemeIdService.getSchemeIds(mockDto, SchemeName.SNOMEDID, "123")).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

        mockMvc.perform(get("/scheme/SNOMEDID/bulk").param("token", "dummy-token").param("schemeIds", "123").with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Internal error"));
    }

    @Test
    void testGetSchemeIds_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/scheme/SNOMEDID/bulk").param("token", "dummy-token").param("schemeIds", "123")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGenerateSchemeIds_validInput_shouldReturnJob() throws Exception {
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();
        // Optionally populate dto

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("testuser").firstName("Test").lastName("User").displayName("Test User").email("test@example.com").roles(List.of("ROLE_USER")).build();

        BulkJob mockJob = new BulkJob();
        // Optionally populate mockJob

        when(bulkSchemeIdService.generateSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockJob);

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "testuser", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())) // ✅ REQUIRED
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    void testGenerateSchemeIds_nullAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testRegisterSchemeIds_validInput_shouldReturnJob() throws Exception {
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("testuser").firstName("Test").lastName("User").displayName("Test User").email("test@example.com").roles(List.of("ROLE_USER")).build();
        BulkJob mockJob = new BulkJob();
        when(bulkSchemeIdService.registerSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockJob);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "testuser", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));
        mockMvc.perform(post("/scheme/SNOMEDID/bulk/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testRegisterSchemeIds_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf()))  // CSRF is present, but no auth
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterSchemeIds_accessDenied_shouldReturnForbidden() throws Exception {
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.registerSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new AccessDeniedException("Access is denied"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isForbidden()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.statusCode").value(403)).andExpect(jsonPath("$.message").value("Access is denied"));
    }


    @Test
    void testRegisterSchemeIds_invalidRequestBody_shouldReturnBadRequest() throws Exception {
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("admin").roles(List.of("ROLE_ADMIN")).build();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        when(bulkSchemeIdService.registerSchemeIds(any(), any(), any())).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid data"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterSchemeIds_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("admin").roles(List.of("ROLE_ADMIN")).build();

        when(bulkSchemeIdService.registerSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Something went wrong"));
    }

    @Test
    void testReserveSchemeIds_accessDenied_shouldReturnForbidden() throws Exception {
        SchemeIdBulkReserveRequestDto dto = new SchemeIdBulkReserveRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.reserveSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new AccessDeniedException("Access is denied"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isForbidden()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.statusCode").value(403)).andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void testReserveSchemeIds_success_shouldReturnOk() throws Exception {
        SchemeIdBulkReserveRequestDto dto = new SchemeIdBulkReserveRequestDto();
        BulkJob bulkJob = new BulkJob();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.reserveSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(bulkJob);

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testReserveSchemeIds_invalidJson_shouldReturnBadRequest() throws Exception {

        String invalidJson = "123";

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }


    @Test
    void testReserveSchemeIds_invalidType_shouldReturnBadRequest() throws Exception {

        String invalidJson = "123";

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void testReleaseSchemeIds_validRequest_shouldReturnOk() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();


        BulkJob mockJob = new BulkJob();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("testuser").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.releaseSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockJob);

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "testuser", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    void testReserveSchemeIds_cisException_shouldReturnInternalServerError() throws Exception {
        SchemeIdBulkReserveRequestDto dto = new SchemeIdBulkReserveRequestDto();

        when(bulkSchemeIdService.reserveSchemeIds(any(), any(), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid scheme data"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Invalid scheme data"));
    }

    @Test
    void testReserveSchemeIds_noAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdBulkReserveRequestDto dto = new SchemeIdBulkReserveRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReserveSchemeIds_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdBulkReserveRequestDto dto = new SchemeIdBulkReserveRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/bulk/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"))))).andExpect(status().isForbidden());
    }

    @Test
    void testDeprecateSchemeIds_success_shouldReturnBulkJob() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();
        BulkJob bulkJob = new BulkJob();
        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        when(bulkSchemeIdService.deprecateSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(bulkJob);

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/deprecate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testDeprecateSchemeIds_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().build();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        when(bulkSchemeIdService.deprecateSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Deprecation failed"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/deprecate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Deprecation failed"));
    }

    private static Stream<Arguments> deprecateSchemeIdScenarios() {

        Authentication validAuth = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        return Stream.of(Arguments.of("Missing token param", null, validAuth, csrf(), 400), Arguments.of("Missing auth", "dummy-token", null, csrf(), 401), Arguments.of("Missing CSRF", "dummy-token", validAuth, null, 403));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("deprecateSchemeIdScenarios")
    void testDeprecateSchemeIds_edgeCases(String name, String token, Authentication auth, RequestPostProcessor csrf, int expectedStatus) throws Exception {

        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        var requestBuilder = put("/scheme/SNOMEDID/bulk/deprecate").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            requestBuilder = requestBuilder.param("token", token);
        }
        if (auth != null) {
            requestBuilder = requestBuilder.with(authentication(auth));
        }
        if (csrf != null) {
            requestBuilder = requestBuilder.with(csrf);
        }

        mockMvc.perform(requestBuilder).andExpect(status().is(expectedStatus));
    }

    @Test
    void testDeprecateSchemeIds_invalidSchemeName_shouldReturnBadRequest() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/INVALID_SCHEME/bulk/deprecate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().is4xxClientError());
    }

    @Test
    void testDeprecateSchemeIds_missingBody_shouldReturnBadRequest() throws Exception {
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/deprecate").param("token", "dummy-token").content("") // Empty body
                .contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testDeprecateSchemeIds_serviceThrowsCisException_shouldReturnError() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().build();
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        when(bulkSchemeIdService.deprecateSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Deprecation failed"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/deprecate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Deprecation failed"));
    }

    @Test
    void testReleaseSchemeIds_nullAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReleaseSchemeIds_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

    @Test
    void testReleaseSchemeIds_invalidJson_shouldReturnBadRequest() throws Exception {
        String invalidJson = "123";

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSchemeIds_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.releaseSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Service failure"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Service failure"));
    }

    @Test
    void testReleaseSchemeIds_missingToken_shouldReturnBadRequest() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/release").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSchemeIds_validRequest_shouldReturnOk() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();
        BulkJob mockJob = new BulkJob();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("testuser").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.publishSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockJob);

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "testuser", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testPublishSchemeIds_missingToken_shouldReturnBadRequest() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSchemeIds_invalidJson_shouldReturnBadRequest() throws Exception {
        String invalidJson = "123";

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void testPublishSchemeIds_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        AuthenticateResponseDto mockDto = AuthenticateResponseDto.builder().name("user").roles(List.of("ROLE_USER")).build();

        when(bulkSchemeIdService.publishSchemeIds(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Publish failed"));

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", mockDto, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Publish failed"));
    }

    @Test
    void testPublishSchemeIds_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testPublishSchemeIds_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdBulkDeprecateRequestDto dto = new SchemeIdBulkDeprecateRequestDto();

        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "user", AuthenticateResponseDto.builder().build(), AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(put("/scheme/SNOMEDID/bulk/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

}
