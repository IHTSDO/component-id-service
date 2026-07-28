package org.snomed.cis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkSctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BulkSctidController.class)
class BulkSctidControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SctidRepository sctidRepository;


    @MockBean
    private BulkSctidService service;

    @MockBean
    private org.snomed.cis.util.SctIdHelper sctIdHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetSctidsByQL_validRequest_shouldReturnList() throws Exception {
        String token = "dummy-token";
        String sctids = "123456,789012";

        Sctid s1 = new Sctid();
        s1.setSctid("123456");

        Sctid s2 = new Sctid();
        s2.setSctid("789012");

        List<Sctid> mockList = List.of(s1, s2);

        when(service.getSctByIds(sctids)).thenReturn(mockList);

        mockMvc.perform(get("/sct/bulk/ids").param("token", token).param("sctids", sctids).accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].sctid").value("123456")).andExpect(jsonPath("$[1].sctid").value("789012"));

    }

    @Test
    void testGetSctidsByQL_emptySctids_shouldReturnEmptyList() throws Exception {
        String token = "dummy-token";
        String sctids = "";

        when(service.getSctByIds(sctids)).thenReturn(List.of());

        mockMvc.perform(get("/sct/bulk/ids").param("token", token).param("sctids", sctids).accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSctidsByQL_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/sct/bulk/ids").param("sctids", "123456").accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidsByQL_missingSctids_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/sct/bulk/ids").param("token", "dummy-token").accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidsByQL_serviceThrowsCisException_shouldReturnInternalServerError() throws Exception {
        String sctids = "123456";
        String token = "dummy-token";

        when(service.getSctByIds(sctids)).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Service failed"));

        mockMvc.perform(get("/sct/bulk/ids").param("token", token).param("sctids", sctids).accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Service failed"));
    }

    @Test
    void testGetSctidsByQL_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/sct/bulk/ids").param("token", "dummy-token").param("sctids", "123456").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSctidsByQL_invalidSctidFormat_shouldStillWorkOrFailBasedOnValidation() throws Exception {
        String sctids = "abc123";
        String token = "dummy-token";

        when(service.getSctByIds(sctids)).thenReturn(List.of());

        mockMvc.perform(get("/sct/bulk/ids").param("token", token).param("sctids", sctids).accept(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSctidsByQLPost_validRequest_shouldReturnList() throws Exception {
        String token = "dummy-token";
        SctIdRequest request = new SctIdRequest();
        request.setSctids(List.of("123456", "789012").toString());

        Sctid s1 = new Sctid();
        s1.setSctid("123456");

        Sctid s2 = new Sctid();
        s2.setSctid("789012");

        List<Sctid> mockList = List.of(s1, s2);
        when(service.postSctByIds(any(SctIdRequest.class))).thenReturn(mockList);

        mockMvc.perform(post("/sct/bulk/ids").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(user("testuser").roles("USER")).with(csrf()))  // ✅ add this
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].sctid").value("123456"));

    }

    @Test
    void testGetSctidsByQLPost_emptySctidList_shouldReturnEmptyList() throws Exception {
        String token = "dummy-token";
        SctIdRequest request = new SctIdRequest();
        request.setSctids(List.of().toString());

        when(service.postSctByIds(any(SctIdRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/sct/bulk/ids").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(user("testuser").roles("USER")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetSctidsByQLPost_missingToken_shouldReturnBadRequest() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids(List.of("123456").toString());

        mockMvc.perform(post("/sct/bulk/ids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(user("testuser").roles("USER")).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidsByQLPost_missingBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/sct/bulk/ids").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(user("testuser").roles("USER")).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidsByQLPost_serviceThrows_shouldReturnInternalServerError() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids(List.of("123456").toString());

        when(service.postSctByIds(any(SctIdRequest.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"));

        mockMvc.perform(post("/sct/bulk/ids").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(user("testuser").roles("USER")).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void testGetSctidsByQLPost_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids(List.of("123456").toString());

        mockMvc.perform(post("/sct/bulk/ids").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());

    }

    @Test
    void testGetSctidBySystemIds_validRequest_shouldReturnList() throws Exception {
        String token = "dummy-token";
        int namespaceId = 12345;
        String systemIds = "sys1,sys2";

        Sctid s1 = new Sctid();
        s1.setSctid("111");
        Sctid s2 = new Sctid();
        s2.setSctid("222");

        when(service.getSctidBySystemIds((systemIds), (namespaceId))).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/sct/namespace/{namespaceId}/systemIds", namespaceId).param("token", token).param("systemIds", systemIds).with(user("testuser").roles("USER")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].sctid").value("111"));
    }

    @Test
    void testGetSctidBySystemIds_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/sct/namespace/12345/systemIds").param("systemIds", "1001,1002").with(user("testuser").roles("USER"))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidBySystemIds_missingSystemIds_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/sct/namespace/12345/systemIds").param("token", "dummy-token").with(user("testuser").roles("USER"))).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSctidBySystemIds_invalidNamespaceId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/sct/namespace/invalid/systemIds").param("token", "dummy-token").param("systemIds", "1001").with(user("testuser").roles("USER"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }


    @Test
    void testGetSctidBySystemIds_serviceThrows_shouldReturn500() throws Exception {
        when(service.getSctidBySystemIds("1001", 12345)).thenThrow(new RuntimeException("Internal Error"));

        mockMvc.perform(get("/sct/namespace/12345/systemIds").param("token", "dummy-token").param("systemIds", "1001").with(user("testuser").roles("USER"))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value(containsString("Internal Error")));
    }


    @Test
    void testGetSctidBySystemIds_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/sct/namespace/12345/systemIds").param("token", "dummy-token").param("systemIds", "1001")).andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterScts_validRequest_shouldReturnBulkJob() throws Exception {
        String token = "dummy-token";
        // Create RegistrationDataDTO using required constructor
        RegistrationDataDTO requestDto = new RegistrationDataDTO(new RegistrationRecordsDTO[]{}, 12345, "TestSoftware", "TestComment");
        BulkJob mockJob = new BulkJob();
        mockJob.setId(101);
        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        when(authDto.toString()).thenReturn("Test User Auth DTO");
        Token mockToken = new Token(token, "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);
        when(service.registerSctids(eq(authDto), any(RegistrationDataDTO.class))).thenReturn(mockJob);
        mockMvc.perform(post("/sct/bulk/register").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(mockToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(101));
    }

    @Test
    void testRegisterScts_unauthenticated_shouldReturn401() throws Exception {
        RegistrationDataDTO requestDto = new RegistrationDataDTO(new RegistrationRecordsDTO[]{new RegistrationRecordsDTO("SCTID123", "SYS001")}, 12345, "TestSoftware", "TestComment");

        mockMvc.perform(post("/sct/bulk/register").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(csrf()))  // No authentication
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterScts_missingToken_shouldReturn400() throws Exception {
        RegistrationDataDTO requestDto = new RegistrationDataDTO(new RegistrationRecordsDTO[]{new RegistrationRecordsDTO("SCTID123", "SYS001")}, 12345, "TestSoftware", "TestComment");

        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), Mockito.mock(AuthenticateResponseDto.class));

        mockMvc.perform(post("/sct/bulk/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(mockToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterScts_missingBody_shouldReturn400() throws Exception {
        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), Mockito.mock(AuthenticateResponseDto.class));

        mockMvc.perform(post("/sct/bulk/register").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(mockToken)).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterScts_serviceThrowsException_shouldReturn500() throws Exception {
        RegistrationDataDTO requestDto = new RegistrationDataDTO(new RegistrationRecordsDTO[]{new RegistrationRecordsDTO("SCTID123", "SYS001")}, 12345, "TestSoftware", "TestComment");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.registerSctids(eq(authDto), any(RegistrationDataDTO.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Something broke"));

        mockMvc.perform(post("/sct/bulk/register").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(mockToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Something broke"));
    }

    @Test
    void testRegisterScts_emptyRecords_shouldStillReturnBulkJobOrBadRequest() throws Exception {
        RegistrationDataDTO requestDto = new RegistrationDataDTO(new RegistrationRecordsDTO[]{}, // Empty
                12345, "TestSoftware", "TestComment");

        BulkJob mockJob = new BulkJob();
        mockJob.setId(202);

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.registerSctids(eq(authDto), any(RegistrationDataDTO.class))).thenReturn(mockJob);

        mockMvc.perform(post("/sct/bulk/register").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(mockToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(202));
    }

    @Test
    void testGenerateSctids_validRequest_shouldReturnBulkJobResponseDto() throws Exception {
        String token = "dummy-token";

        // Prepare mock input DTO
        SCTIDBulkGenerationRequestDto requestDto = new SCTIDBulkGenerationRequestDto();
        requestDto.setNamespace(12345);
        requestDto.setPartitionId("01");
        requestDto.setQuantity(5);
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Generate test");

        BulkJob mainJob = new BulkJob();
        mainJob.setId(301);
        mainJob.setName("PrimaryJob");
        mainJob.setStatus("QUEUED");

        List<BulkJob> additionalJobs = List.of();

        BulkJobResponseDto responseDto = new BulkJobResponseDto(mainJob, additionalJobs);
        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        when(authDto.toString()).thenReturn("Test User DTO");

        Token mockToken = new Token(token, "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.generateSctids(eq(authDto), any(SCTIDBulkGenerationRequestDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/sct/bulk/generate").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(mockToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(301)).andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testGenerateSctids_unauthenticated_shouldReturn401() throws Exception {
        SCTIDBulkGenerationRequestDto requestDto = new SCTIDBulkGenerationRequestDto(12345, "01", 5, null, "TestSoftware", "comment", "false");

        mockMvc.perform(post("/sct/bulk/generate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testGenerateSctids_missingToken_shouldReturn400() throws Exception {
        SCTIDBulkGenerationRequestDto requestDto = new SCTIDBulkGenerationRequestDto(12345, "01", 5, null, "TestSoftware", "comment", "false");

        mockMvc.perform(post("/sct/bulk/generate").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateSctids_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/sct/bulk/generate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateSctids_invalidRequest_missingFields_shouldReturn400() throws Exception {
        SCTIDBulkGenerationRequestDto requestDto = new SCTIDBulkGenerationRequestDto();

        mockMvc.perform(post("/sct/bulk/generate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    private Token getMockToken() {
        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        return new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);
    }

    @Test
    void testGenerateSctids_serviceThrowsException_shouldReturn500() throws Exception {
        SCTIDBulkGenerationRequestDto requestDto = new SCTIDBulkGenerationRequestDto(12345, "01", 5, null, "TestSoftware", "comment", "false");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token tokenObj = new Token("dummy-token", "user", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.generateSctids(eq(authDto), any(SCTIDBulkGenerationRequestDto.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Generation failed"));

        mockMvc.perform(post("/sct/bulk/generate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)).with(authentication(tokenObj)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Generation failed"));
    }

    @Test
    void testDeprecateSctid_validRequest_shouldReturnBulkJob() throws Exception {
        String token = "dummy-token";

        BulkSctRequestDTO deprecationData = new BulkSctRequestDTO(new String[]{"SCTID123", "SCTID456"}, 12345, "TestSoftware", "Mark for deprecation");
        BulkJob mockJob = new BulkJob();
        mockJob.setId(401);
        mockJob.setStatus("QUEUED");
        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token(token, "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);
        when(service.deprecateSctid(eq(authDto), any(BulkSctRequestDTO.class))).thenReturn(mockJob);
        mockMvc.perform(put("/sct/bulk/deprecate").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(deprecationData)).with(authentication(mockToken)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(401)).andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testDeprecateSctid_unauthenticated_shouldReturn401() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID123"}, 12345, "TestSoftware", "comment");

        mockMvc.perform(put("/sct/bulk/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testDeprecateSctid_missingToken_shouldReturn400() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID123"}, 12345, "TestSoftware", "comment");

        mockMvc.perform(put("/sct/bulk/deprecate").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testDeprecateSctid_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(put("/sct/bulk/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testDeprecateSctid_invalidDTO_shouldReturn200_ifValidationNotHandled() throws Exception {
        BulkSctRequestDTO invalidRequest = new BulkSctRequestDTO(new String[]{},  // empty sctids
                null, null, null);

        BulkJob mockJob = new BulkJob();
        mockJob.setId(777);
        mockJob.setStatus("QUEUED");

        when(service.deprecateSctid(any(), any())).thenReturn(mockJob);

        mockMvc.perform(put("/sct/bulk/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(777));
    }

    @Test
    void testDeprecateSctid_serviceThrowsException_shouldReturn500() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID999"}, 12345, "TestSoftware", "Deprecate invalid");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.deprecateSctid(eq(authDto), any(BulkSctRequestDTO.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Deprecation failed"));

        mockMvc.perform(put("/sct/bulk/deprecate").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(mockToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Deprecation failed"));
    }

    @Test
    void testPublishSctid_validRequest_shouldReturnBulkJob() throws Exception {
        String token = "dummy-token";

        BulkSctRequestDTO publishRequest = new BulkSctRequestDTO(new String[]{"SCTID789", "SCTID456"}, 12345, "TestSoftware", "Publishing now");

        BulkJob mockJob = new BulkJob();
        mockJob.setId(501);
        mockJob.setStatus("QUEUED");

        when(service.publishSctid(any(), any())).thenReturn(mockJob);

        mockMvc.perform(put("/sct/bulk/publish").param("token", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(publishRequest)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(501)).andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testPublishSctid_unauthenticated_shouldReturn401() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID789"}, 12345, "TestSoftware", "Comment");

        mockMvc.perform(put("/sct/bulk/publish").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testPublishSctid_missingToken_shouldReturn400() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID789"}, 12345, "TestSoftware", "Comment");

        mockMvc.perform(put("/sct/bulk/publish").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSctid_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(put("/sct/bulk/publish").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testPublishSctid_serviceThrowsException_shouldReturn500() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID789"}, 12345, "TestSoftware", "Fail me");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token("dummy-token", "user", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.publishSctid(eq(authDto), any(BulkSctRequestDTO.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Publish failed"));

        mockMvc.perform(put("/sct/bulk/publish").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(mockToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Publish failed"));
    }

    @Test
    void testPublishSctid_invalidDTO_shouldStillReturn200_ifNoValidation() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{}, null, null, null);

        BulkJob mockJob = new BulkJob();
        mockJob.setId(502);

        when(service.publishSctid(any(), any())).thenReturn(mockJob);

        mockMvc.perform(put("/sct/bulk/publish").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(502));
    }

    @Test
    void testReleaseSctid_validRequest_shouldReturnBulkJob() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID999"}, 12345, "TestSoftware", "Release note");

        BulkJob mockJob = new BulkJob();
        mockJob.setId(601);
        mockJob.setStatus("QUEUED");

        when(service.releaseSctid(any(), any())).thenReturn(mockJob);

        mockMvc.perform(put("/sct/bulk/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(601)).andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testReleaseSctid_unauthenticated_shouldReturn401() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID999"}, 12345, "TestSoftware", "Release note");

        mockMvc.perform(put("/sct/bulk/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReleaseSctid_missingToken_shouldReturn400() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID999"}, 12345, "TestSoftware", "Release note");

        mockMvc.perform(put("/sct/bulk/release").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSctid_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(put("/sct/bulk/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSctid_serviceThrowsException_shouldReturn500() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{"SCTID999"}, 12345, "TestSoftware", "Failing release");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token mockToken = new Token("dummy-token", "testuser", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.releaseSctid(eq(authDto), any(BulkSctRequestDTO.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Release failed"));

        mockMvc.perform(put("/sct/bulk/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(mockToken)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Release failed"));
    }

    @Test
    void testReleaseSctid_invalidDTO_shouldStillReturn200() throws Exception {
        BulkSctRequestDTO request = new BulkSctRequestDTO(new String[]{}, null, null, null);

        BulkJob mockJob = new BulkJob();
        mockJob.setId(602);

        when(service.releaseSctid(any(), any())).thenReturn(mockJob);

        mockMvc.perform(put("/sct/bulk/release").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(602));
    }

    @Test
    void testReserveSctids_validRequest_shouldReturnBulkJob() throws Exception {
        SCTIDBulkReservationRequestDto request = new SCTIDBulkReservationRequestDto();
        request.setNamespace(12345);
        request.setPartitionId("01");
        request.setQuantity(10);
        request.setSoftware("TestSoftware");
        request.setComment("Reserve note");


        BulkJob mockJob = new BulkJob();
        mockJob.setId(701);
        mockJob.setStatus("QUEUED");

        when(service.reserveSctids(any(), any())).thenReturn(mockJob);

        mockMvc.perform(post("/sct/bulk/reserve").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(701)).andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testReserveSctids_unauthenticated_shouldReturn401() throws Exception {
        SCTIDBulkReservationRequestDto request = new SCTIDBulkReservationRequestDto();
        request.setNamespace(12345);
        request.setPartitionId("01");
        request.setQuantity(10);
        request.setSoftware("TestSoftware");
        request.setComment("Reserve note");


        mockMvc.perform(post("/sct/bulk/reserve").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void testReserveSctids_missingToken_shouldReturn400() throws Exception {
        SCTIDBulkReservationRequestDto request = new SCTIDBulkReservationRequestDto();
        request.setNamespace(12345);
        request.setPartitionId("01");
        request.setQuantity(10);
        request.setSoftware("TestSoftware");
        request.setComment("Reserve note");


        mockMvc.perform(post("/sct/bulk/reserve").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReserveSctids_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/sct/bulk/reserve").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReserveSctids_invalidBody_shouldReturn400() throws Exception {
        SCTIDBulkReservationRequestDto invalidRequest = new SCTIDBulkReservationRequestDto();
        invalidRequest.setNamespace(null);
        invalidRequest.setPartitionId(null);
        invalidRequest.setQuantity(null);
        invalidRequest.setSoftware(null);
        invalidRequest.setComment(null);


        mockMvc.perform(post("/sct/bulk/reserve").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest)).with(authentication(getMockToken())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    void testReserveSctids_serviceThrows_shouldReturn500() throws Exception {
        SCTIDBulkReservationRequestDto request = new SCTIDBulkReservationRequestDto();
        request.setNamespace(12345);
        request.setPartitionId("01");
        request.setQuantity(10);
        request.setSoftware("TestSoftware");
        request.setComment("Error cause");

        AuthenticateResponseDto authDto = Mockito.mock(AuthenticateResponseDto.class);
        Token tokenObj = new Token("dummy-token", "user", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), authDto);

        when(service.reserveSctids(eq(authDto), any(SCTIDBulkReservationRequestDto.class))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Reserve failed"));

        mockMvc.perform(post("/sct/bulk/reserve").param("token", "dummy-token").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(authentication(tokenObj)).with(csrf())).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Reserve failed"));
    }

}
