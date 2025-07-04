package org.snomed.cis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchemeIdController.class)
class SchemeIdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemeIdService schemeIdService;

    @Autowired
    private ObjectMapper objectMapper;

    private Token authToken;
    private AuthenticateResponseDto authDto;

    @BeforeEach
    void setup() {
        authDto = AuthenticateResponseDto.builder().email("test@example.com").roles(List.of("ROLE_USER")).build();

        authToken = new Token("dummy-token", "testuser", true, List.of(), authDto);
    }

    @Test
    void testGetSchemeIds_success_shouldReturnList() throws Exception {
        SchemeId scheme1 = Mockito.mock(SchemeId.class);
        SchemeId scheme2 = Mockito.mock(SchemeId.class);
        when(scheme1.getSchemeId()).thenReturn("SNOMEDID_1");
        when(scheme2.getSchemeId()).thenReturn("SNOMEDID_2");

        List<SchemeId> schemeIds = List.of(scheme1, scheme2);

        when(schemeIdService.getSchemeIds(authDto, "10", "0", SchemeName.SNOMEDID)).thenReturn(schemeIds);


        mockMvc.perform(get("/scheme/ids").param("token", "dummy-token").param("limit", "10").param("skip", "0").param("scheme", "SNOMEDID").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].schemeId").value("SNOMEDID_1")).andExpect(jsonPath("$[1].schemeId").value("SNOMEDID_2"));
    }


    @Test
    void testGetSchemeIds_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/scheme/ids").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSchemeIds_invalidScheme_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/scheme/ids").param("token", "dummy-token").param("scheme", "INVALID").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSchemeIds_serviceThrowsException_shouldReturnServerError() throws Exception {
        when(schemeIdService.getSchemeIds(any(), any(), any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/scheme/ids").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSchemeIds_noResults_shouldReturnEmptyList() throws Exception {
        when(schemeIdService.getSchemeIds(eq(authDto), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/scheme/ids").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void testGetSchemeId_success_shouldReturnSchemeId() throws Exception {
        SchemeId mockSchemeId = Mockito.mock(SchemeId.class);
        when(mockSchemeId.getSchemeId()).thenReturn("SNOMEDID_1");

        when(schemeIdService.getSchemeId((authDto), (SchemeName.SNOMEDID), ("SNOMEDID_1"))).thenReturn(mockSchemeId);

        mockMvc.perform(get("/scheme/SNOMEDID/ids/SNOMEDID_1").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.schemeId").value("SNOMEDID_1"));
    }

    @Test
    void testGetSchemeId_notFound_shouldReturnInternalServerError() throws Exception {
        when(schemeIdService.getSchemeId((authDto), (SchemeName.SNOMEDID), ("UNKNOWN_ID"))).thenThrow(new RuntimeException("Scheme ID not found"));

        mockMvc.perform(get("/scheme/SNOMEDID/ids/UNKNOWN_ID").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/scheme/SNOMEDID/ids/SNOMEDID_1").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSchemeId_invalidSchemeName_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/scheme/INVALID_SCHEME/ids/SNOMEDID_1").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSchemeId_cisExceptionBadRequest_shouldReturn400() throws Exception {
        when(schemeIdService.getSchemeId((authDto), (SchemeName.SNOMEDID), ("INVALID_ID"))).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid Scheme ID"));

        mockMvc.perform(get("/scheme/SNOMEDID/ids/INVALID_ID").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid Scheme ID"));
    }

    @Test
    void testGetSchemeId_missingTokenParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/scheme/SNOMEDID/ids/SNOMEDID_1").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSchemeId_unexpectedError_shouldReturn500() throws Exception {
        when(schemeIdService.getSchemeId((authDto), (SchemeName.SNOMEDID), ("SNOMEDID_1"))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/scheme/SNOMEDID/ids/SNOMEDID_1").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSchemeIdBySystemId_success_shouldReturnSchemeId() throws Exception {
        SchemeId mockSchemeId = new SchemeId();
        mockSchemeId.setScheme("SNOMEDID");
        mockSchemeId.setSystemId("SYS123");

        when(schemeIdService.getSchemeIdsBySystemId((authDto), (SchemeName.SNOMEDID), ("SYS123"))).thenReturn(mockSchemeId);

        mockMvc.perform(get("/scheme/SNOMEDID/systemids/SYS123").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$.scheme").value("SNOMEDID")).andExpect(jsonPath("$.systemId").value("SYS123"));
    }

    @Test
    void testGetSchemeIdBySystemId_unauthorized_shouldReturn401() throws Exception {
        mockMvc.perform(get("/scheme/SNOMEDID/systemids/SYS123").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSchemeIdBySystemId_invalidSchemeName_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/scheme/INVALID/systemids/SYS123").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSchemeIdBySystemId_notFound_shouldReturn404() throws Exception {
        when(schemeIdService.getSchemeIdsBySystemId((authDto), (SchemeName.SNOMEDID), ("UNKNOWN"))).thenThrow(new CisException(HttpStatus.NOT_FOUND, "System ID not found"));

        mockMvc.perform(get("/scheme/SNOMEDID/systemids/UNKNOWN").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("System ID not found"));
    }

    @Test
    void testGetSchemeIdBySystemId_internalError_shouldReturn500() throws Exception {
        when(schemeIdService.getSchemeIdsBySystemId((authDto), (SchemeName.SNOMEDID), ("SYS123"))).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/scheme/SNOMEDID/systemids/SYS123").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError());
    }

    @Test
    void testDeprecateSchemeId_success_shouldReturnSchemeId() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID123");
        updateDto.setAuthor("Test Author");
        updateDto.setSoftware("Test Software");
        updateDto.setComment("Deprecated for testing");

        SchemeId mockResponse = SchemeId.builder().scheme("SNOMEDID").schemeId("SCTID123").status("DEPRECATED").author("Test Author").software("Test Software").comment("Deprecated for testing").build();

        when(schemeIdService.deprecateSchemeIds(eq(authDto), eq(SchemeName.SNOMEDID), any(SchemeIdUpdateRequestDto.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/scheme/SNOMEDID/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.scheme").value("SNOMEDID")).andExpect(jsonPath("$.schemeId").value("SCTID123")).andExpect(jsonPath("$.status").value("DEPRECATED")).andExpect(jsonPath("$.author").value("Test Author")).andExpect(jsonPath("$.software").value("Test Software")).andExpect(jsonPath("$.comment").value("Deprecated for testing"));
    }

    @Test
    void testDeprecateSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID123");

        mockMvc.perform(put("/scheme/SNOMEDID/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testDeprecateSchemeId_invalidJson_shouldReturnBadRequest() throws Exception {
        String invalidJson = "{schemeId:SCTID123"; // malformed JSON

        mockMvc.perform(put("/scheme/SNOMEDID/deprecate").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testDeprecateSchemeId_serviceThrowsException_shouldReturn500() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID123");

        when(schemeIdService.deprecateSchemeIds(any(), any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/scheme/SNOMEDID/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError());
    }

    @Test
    void testDeprecateSchemeId_missingOptionalFields_shouldSucceed() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID123");

        SchemeId mockResponse = SchemeId.builder().scheme("SNOMEDID").schemeId("SCTID123").status("DEPRECATED").build();

        when(schemeIdService.deprecateSchemeIds(eq(authDto), eq(SchemeName.SNOMEDID), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/scheme/SNOMEDID/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.scheme").value("SNOMEDID")).andExpect(jsonPath("$.schemeId").value("SCTID123")).andExpect(jsonPath("$.status").value("DEPRECATED"));
    }

    @Test
    void testReleaseSchemeId_success_shouldReturnSchemeId() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");
        updateDto.setAuthor("Release Author");
        updateDto.setSoftware("Release Software");
        updateDto.setComment("Released for use");

        SchemeId mockResponse = SchemeId.builder().scheme("SNOMEDID").schemeId("SCTID456").status("RELEASED").author("Release Author").software("Release Software").comment("Released for use").build();

        when(schemeIdService.releaseSchemeIds(eq(authDto), eq(SchemeName.SNOMEDID), any(SchemeIdUpdateRequestDto.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.scheme").value("SNOMEDID")).andExpect(jsonPath("$.schemeId").value("SCTID456")).andExpect(jsonPath("$.status").value("RELEASED")).andExpect(jsonPath("$.author").value("Release Author")).andExpect(jsonPath("$.software").value("Release Software")).andExpect(jsonPath("$.comment").value("Released for use"));
    }

    @Test
    void testReleaseSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReleaseSchemeId_serviceThrowsException_shouldReturnServerError() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        when(schemeIdService.releaseSchemeIds(any(), any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError());
    }

    @Test
    void testReleaseSchemeId_invalidJson_shouldReturnBadRequest() throws Exception {
        String invalidJson = "{schemeId:SCTID456";

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(invalidJson).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSchemeId_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken))) // No csrf()
                .andExpect(status().isForbidden());
    }

    @Test
    void testReleaseSchemeId_invalidToken_shouldReturnForbidden() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        Token unauthorizedToken = new Token("invalid-token", "baduser", true, List.of(), authDto);

        when(schemeIdService.releaseSchemeIds(any(), any(), any())).thenThrow(new AccessDeniedException("Invalid token"));

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "invalid-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(unauthorizedToken)).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void testReleaseSchemeId_emptyRequestBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content("").with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSchemeId_nullResponse_shouldReturnOkWithEmptyBody() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        when(schemeIdService.releaseSchemeIds(eq(authDto), eq(SchemeName.SNOMEDID), any())).thenReturn(null);

        mockMvc.perform(put("/scheme/SNOMEDID/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    void testReleaseSchemeId_invalidSchemeName_shouldReturnBadRequest() throws Exception {
        SchemeIdUpdateRequestDto updateDto = new SchemeIdUpdateRequestDto();
        updateDto.setSchemeId("SCTID456");

        mockMvc.perform(put("/scheme/INVALID_SCHEME/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    SchemeIdUpdateRequestDto validDto = new SchemeIdUpdateRequestDto();

    @Test
    void testPublishSchemeId_validRequest_shouldReturnSchemeId() throws Exception {
        SchemeId mockResponse = new SchemeId();

        when(schemeIdService.publishSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/scheme/SNOMEDID/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testPublishSchemeId_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/scheme/SNOMEDID/publish").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/scheme/SNOMEDID/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testPublishSchemeId_missingCsrf_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/scheme/SNOMEDID/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

    @Test
    void testPublishSchemeId_invalidJson_shouldReturnBadRequest() throws Exception {
        String invalidJson = "123";

        mockMvc.perform(put("/scheme/SNOMEDID/publish").param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSchemeId_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        when(schemeIdService.publishSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Publish failed"));

        mockMvc.perform(put("/scheme/SNOMEDID/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Publish failed"));
    }

    @Test
    void testPublishSchemeId_invalidSchemeName_shouldReturn4xx() throws Exception {
        mockMvc.perform(put("/scheme/INVALID_SCHEME/publish").param("token", "dummy-token").content(objectMapper.writeValueAsString(validDto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().is4xxClientError());
    }

    @Test
    void testReserveSchemeId_validRequest_shouldReturnSchemeId() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();
        SchemeId mockResponse = new SchemeId();
        when(schemeIdService.reserveSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockResponse);
        mockMvc.perform(post("/scheme/SNOMEDID/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testReserveSchemeId_missingToken_shouldReturnBadRequest() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/reserve").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReserveSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReserveSchemeId_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();
        mockMvc.perform(post("/scheme/SNOMEDID/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

    @Test
    void testReserveSchemeId_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();
        when(schemeIdService.reserveSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Something failed"));
        mockMvc.perform(post("/scheme/SNOMEDID/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Something failed"));
    }

    @Test
    void testReserveSchemeId_invalidSchemeName_shouldReturn4xx() throws Exception {
        SchemeIdReserveRequestDto dto = new SchemeIdReserveRequestDto();
        mockMvc.perform(post("/scheme/INVALID_SCHEME/reserve").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().is4xxClientError());
    }

    @Test
    void testGenerateSchemeId_validRequest_shouldReturnSchemeId() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();
        SchemeId mockResponse = new SchemeId();
        when(schemeIdService.generateSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/scheme/SNOMEDID/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGenerateSchemeId_missingToken_shouldReturnBadRequest() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();
        mockMvc.perform(post("/scheme/SNOMEDID/generate").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testGenerateSchemeId_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

    @Test
    void testGenerateSchemeId_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();
        when(schemeIdService.generateSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Generation failed"));

        mockMvc.perform(post("/scheme/SNOMEDID/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("Generation failed"));
    }

    @Test
    void testGenerateSchemeId_invalidSchemeName_shouldReturn4xx() throws Exception {
        SchemeIdGenerateRequestDto dto = new SchemeIdGenerateRequestDto();


        mockMvc.perform(post("/scheme/INVALID_SCHEME/generate").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().is4xxClientError()); // likely 400
    }

    @Test
    void testRegisterSchemeId_validRequest_shouldReturnOk() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto(); // populate as needed
        SchemeId mockResponse = new SchemeId();

        when(schemeIdService.registerSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenReturn(mockResponse);


        mockMvc.perform(post("/scheme/SNOMEDID/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testRegisterSchemeId_missingToken_shouldReturnBadRequest() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/register").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterSchemeId_missingAuthentication_shouldReturnUnauthorized() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto();

        mockMvc.perform(post("/scheme/SNOMEDID/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterSchemeId_missingCsrf_shouldReturnForbidden() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto();


        mockMvc.perform(post("/scheme/SNOMEDID/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken))).andExpect(status().isForbidden());
    }

    private static Stream<Arguments> invalidJsonEndpoints() {
        return Stream.of(Arguments.of("/scheme/SNOMEDID/register"), Arguments.of("/scheme/SNOMEDID/generate"), Arguments.of("/scheme/SNOMEDID/reserve"));
    }

    @ParameterizedTest(name = "Invalid JSON should return 400 for {0}")
    @MethodSource("invalidJsonEndpoints")
    void testInvalidJson_shouldReturnBadRequest(String endpoint) throws Exception {
        String invalidJson = "123"; // not a JSON object

        mockMvc.perform(post(endpoint).param("token", "dummy-token").content(invalidJson).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterSchemeId_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto();


        when(schemeIdService.registerSchemeId(any(), eq(SchemeName.SNOMEDID), any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed"));


        mockMvc.perform(post("/scheme/SNOMEDID/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Registration failed"));
    }

    @Test
    void testRegisterSchemeId_invalidSchemeName_shouldReturnClientError() throws Exception {
        SchemeIdRegisterRequestDto dto = new SchemeIdRegisterRequestDto();


        mockMvc.perform(post("/scheme/INVALID/register").param("token", "dummy-token").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON).with(authentication(authToken)).with(csrf())).andExpect(status().is4xxClientError());
    }

}
