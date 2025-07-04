package org.snomed.cis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SctidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class SctidControllerTest {

    @InjectMocks
    private SctidController sctidController;

    @Mock
    private SctidService sctidService;

    @Mock
    private Token token;
    @Mock
    private AuthenticateResponseDto authenticateResponseDto;

    @Mock
    private SctWithSchemeResponseDTO sctWithSchemeResponseDTO;

    @Mock
    private CheckSctidResponseDTO checkSctidResponseDTO;
    @Mock
    private Sctid sctid;

    @Mock
    private Sctid sctidResult;

    @Mock
    private DeprecateSctRequestDTO deprecateRequestDTO;

    @Mock
    private Sctid deprecatedSctid;

    @Mock
    private AuthenticateResponseDto authDto;
    @Mock
    private SctidsGenerateRequestDto generationData;

    @Mock
    private SCTIDRegistrationRequest registrationRequest;
    @Mock
    private SCTIDReservationRequest reservationRequest;
    @Mock
    private DeprecateSctRequestDTO deprecateRequest;
    @Mock
    private Sctid releasedSctid;
    @Mock
    private Sctid sctid1, sctid2;

    private final String dummyToken = "dummy";

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
    }

    @Test
    void testGetSct_SuccessAllParams() throws CisException {
        String limit = "10", skip = "5", namespace = "100001";
        List<Sctid> sctids = List.of(sctid1, sctid2);

        Mockito.when(sctidService.getSct(authenticateResponseDto, limit, skip, namespace)).thenReturn(sctids);

        ResponseEntity<List<Sctid>> response = sctidController.getSct(dummyToken, limit, skip, namespace, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctids, response.getBody());
    }

    @Test
    void testGetSct_NullOptionalParams() throws CisException {
        List<Sctid> sctids = List.of(sctid1);

        Mockito.when(sctidService.getSct(authenticateResponseDto, null, null, null)).thenReturn(sctids);

        ResponseEntity<List<Sctid>> response = sctidController.getSct(dummyToken, null, null, null, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctids, response.getBody());
    }

    @Test
    void testGetSct_ServiceThrowsException() throws CisException {
        String limit = "10", skip = "0", namespace = "999";

        Mockito.when(sctidService.getSct(authenticateResponseDto, limit, skip, namespace)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid namespace"));

        CisException exception = assertThrows(CisException.class, () -> sctidController.getSct(dummyToken, limit, skip, namespace, token));

        assertEquals("Invalid namespace", exception.getMessage());
    }

    @Test
    void testGetSct_AuthenticationCastFails() {
        Authentication fakeAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> sctidController.getSct(dummyToken, "10", "0", "123", fakeAuth));
    }

    @Test
    void testGetSctWithId_Success() throws CisException {

        String includeAdditionalIds = "true";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.getSctWithId(authenticateResponseDto, "12345678", includeAdditionalIds)).thenReturn(sctWithSchemeResponseDTO);

        ResponseEntity<SctWithSchemeResponseDTO> response = sctidController.getSctWithId("dummyToken", "12345678", includeAdditionalIds, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctWithSchemeResponseDTO, response.getBody());
    }

    @Test
    void testGetSctWithId_NullOptionalParam() throws CisException {


        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.getSctWithId(authenticateResponseDto, "12345678", null)).thenReturn(sctWithSchemeResponseDTO);

        ResponseEntity<SctWithSchemeResponseDTO> response = sctidController.getSctWithId("dummyToken", "12345678", null, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctWithSchemeResponseDTO, response.getBody());
    }

    @Test
    void testGetSctWithId_ServiceThrowsException() throws CisException {

        String includeAdditionalIds = "false";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.getSctWithId(authenticateResponseDto, "invalid", includeAdditionalIds)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "SCTID not found"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.getSctWithId("dummyToken", "invalid", includeAdditionalIds, token));

        assertEquals("SCTID not found", ex.getMessage());
    }

    @Test
    void testGetSctWithId_AuthenticationCastFails() {
        Authentication fakeAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> sctidController.getSctWithId("dummyToken", "12345678", "true", fakeAuth));
    }

    @Test
    void testCheckSctid_Success() throws CisException {

        Mockito.when(sctidService.checkSctid("12345678")).thenReturn(checkSctidResponseDTO);

        ResponseEntity<CheckSctidResponseDTO> response = sctidController.checkSctid(null, "12345678");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(checkSctidResponseDTO, response.getBody());
    }

    @Test
    void testCheckSctid_ThrowsException() throws CisException {

        Mockito.when(sctidService.checkSctid("bad-id")).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid SCTID"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.checkSctid(null, "bad-id"));

        assertEquals("Invalid SCTID", ex.getMessage());
    }

    @Test
    void testGetSctBySystemId_Success() throws CisException {
        Integer namespaceId = 100001;
        String systemId = "ABC123";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.getSctWithSystemId(authenticateResponseDto, namespaceId, systemId)).thenReturn(sctidResult);

        ResponseEntity<Sctid> response = sctidController.getSctBySystemId("dummyToken", namespaceId, systemId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctidResult, response.getBody());
    }

    @Test
    void testGetSctBySystemId_ServiceThrowsException() throws CisException {
        Integer namespaceId = 100002;
        String systemId = "INVALID";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.getSctWithSystemId(authenticateResponseDto, namespaceId, systemId)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "System ID not found"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.getSctBySystemId("dummyToken", namespaceId, systemId, token));

        assertEquals("System ID not found", ex.getMessage());
    }

    @Test
    void testGetSctBySystemId_AuthenticationCastFails() {
        Integer namespaceId = 100003;
        String systemId = "XYZ789";
        Authentication fakeAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> sctidController.getSctBySystemId("dummyToken", namespaceId, systemId, fakeAuth));
    }

    @Test
    void testDeprecateSctid_Success() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.deprecateSct(authenticateResponseDto, deprecateRequestDTO)).thenReturn(deprecatedSctid);

        ResponseEntity<Sctid> response = sctidController.deprecateSctid("dummyToken", deprecateRequestDTO, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deprecatedSctid, response.getBody());
    }

    @Test
    void testDeprecateSctid_ThrowsException() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.deprecateSct(authenticateResponseDto, deprecateRequestDTO)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Deprecation failed"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.deprecateSctid("dummyToken", deprecateRequestDTO, token));

        assertEquals("Deprecation failed", ex.getMessage());
    }

    @Test
    void testDeprecateSctid_InvalidAuthentication() {
        Authentication fakeAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> sctidController.deprecateSctid("dummyToken", deprecateRequestDTO, fakeAuth));
    }

    @Test
    void testReleaseSctid_Success() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.releaseSct(authenticateResponseDto, deprecateRequest)).thenReturn(releasedSctid);

        ResponseEntity<Sctid> response = sctidController.releaseSctid("dummyToken", deprecateRequest, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(releasedSctid, response.getBody());
    }

    @Test
    void testReleaseSctid_ServiceThrowsException() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(sctidService.releaseSct(authenticateResponseDto, deprecateRequest)).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Release failed"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.releaseSctid("dummyToken", deprecateRequest, token));

        assertEquals("Release failed", ex.getMessage());
    }

    @Test
    void testReleaseSctid_InvalidAuthentication() {
        Authentication fakeAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> sctidController.releaseSctid("dummyToken", deprecateRequest, fakeAuth));
    }

    @Test
    void testGenerateSctid_ThrowsException() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.generateSctid(authDto, generationData)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Generate failed"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.generateSctid("token", generationData, token));

        assertEquals("Generate failed", ex.getMessage());
    }

    @Test
    void testReserveSctid_ThrowsException() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Reservation failed"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.reserveSctid("token", reservationRequest, token));

        assertEquals("Reservation failed", ex.getMessage());
    }

    @Test
    void testGenerateSctid_Success() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.generateSctid(authDto, generationData)).thenReturn(sctWithSchemeResponseDTO);

        ResponseEntity<SctWithSchemeResponseDTO> response = sctidController.generateSctid("token", generationData, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctWithSchemeResponseDTO, response.getBody());
    }

    @ParameterizedTest
    @MethodSource("provideSctidOperationScenarios")
    void testSctidOperations(String operationType, boolean shouldThrowException, Object expectedResponse, String expectedMessage) throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);

        switch (operationType) {
            case "generate":
                if (shouldThrowException) {
                    Mockito.when(sctidService.generateSctid(authDto, generationData)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, expectedMessage));

                    CisException ex = assertThrows(CisException.class, () -> sctidController.generateSctid("token", generationData, token));
                    assertEquals(expectedMessage, ex.getMessage());
                } else {
                    Mockito.when(sctidService.generateSctid(authDto, generationData)).thenReturn((SctWithSchemeResponseDTO) expectedResponse);

                    ResponseEntity<SctWithSchemeResponseDTO> response = sctidController.generateSctid("token", generationData, token);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedResponse, response.getBody());
                }
                break;

            case "reserve":
                if (shouldThrowException) {
                    Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, expectedMessage));

                    CisException ex = assertThrows(CisException.class, () -> sctidController.reserveSctid("token", reservationRequest, token));
                    assertEquals(expectedMessage, ex.getMessage());
                } else {
                    Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenReturn((Sctid) expectedResponse);

                    ResponseEntity<Sctid> response = sctidController.reserveSctid("token", reservationRequest, token);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedResponse, response.getBody());
                }
                break;
            default:
                fail("Unsupported operation type: " + operationType);
        }
    }

    private static Stream<Arguments> provideSctidOperationScenarios() {
        SctWithSchemeResponseDTO sctResponse = new SctWithSchemeResponseDTO();
        Sctid reservedSctid = new Sctid();
        reservedSctid.setSctid("123456789");

        return Stream.of(Arguments.of("generate", false, sctResponse, null), Arguments.of("generate", true, null, "Generate failed"), Arguments.of("reserve", true, null, "Reservation failed"), Arguments.of("reserve", false, reservedSctid, null));
    }


    @Test
    void testRegisterSctid_Success_Second() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.registerSctid(authDto, registrationRequest)).thenReturn(sctid);

        ResponseEntity<Sctid> response = sctidController.registerSctid("token", registrationRequest, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctid, response.getBody());
    }

    @Test
    void testRegisterSctid_ThrowsException_Second() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.registerSctid(authDto, registrationRequest)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Registration failed"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.registerSctid("token", registrationRequest, token));

        assertEquals("Registration failed", ex.getMessage());
    }

    @Test
    void testReserveSctid_Success_Second() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenReturn(sctid);

        ResponseEntity<Sctid> response = sctidController.reserveSctid("token", reservationRequest, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctid, response.getBody());
    }


    private static Stream<Arguments> provideRegisterSctidScenarios() {
        Sctid mockSctid = new Sctid();
        mockSctid.setSctid("123456789");

        return Stream.of(arguments(false, mockSctid, null), arguments(true, null, "Registration failed"));
    }

    @ParameterizedTest
    @MethodSource("provideRegisterSctidScenarios")
    void testRegisterSctid_Scenarios(boolean shouldThrowException, Sctid expectedResponse, String expectedMessage) throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);

        if (shouldThrowException) {
            Mockito.when(sctidService.registerSctid(authDto, registrationRequest)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, expectedMessage));

            CisException ex = assertThrows(CisException.class, () -> sctidController.registerSctid("token", registrationRequest, token));
            assertEquals(expectedMessage, ex.getMessage());

        } else {
            Mockito.when(sctidService.registerSctid(authDto, registrationRequest)).thenReturn(expectedResponse);

            ResponseEntity<Sctid> response = sctidController.registerSctid("token", registrationRequest, token);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }


    @Test
    void testReserveSctid_Success200_WithDifferentNamespace() throws CisException {
        reservationRequest.setNamespace(123456);
        reservationRequest.setPartitionId("01");
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenReturn(sctid);
        ResponseEntity<Sctid> response = sctidController.reserveSctid("token", reservationRequest, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sctid, response.getBody());
    }

    @Test
    void testReserveSctid_Failure_dueToInvalidNamespace() throws CisException {
        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authDto);
        Mockito.when(sctidService.reserveSctid(authDto, reservationRequest)).thenThrow(new CisException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid namespace ID"));

        CisException ex = assertThrows(CisException.class, () -> sctidController.reserveSctid("token", reservationRequest, token));

        assertEquals("Invalid namespace ID", ex.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    }


}


