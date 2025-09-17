package org.snomed.cis.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @InjectMocks
    private StatsController statsController;

    @Mock
    private StatsService statsService;

    @Mock
    private Token token;

    @Mock
    private AuthenticateResponseDto authenticateResponseDto;

    @Mock
    private GetStatsResponseDto mockResponse;

    @Test
    void testGetStats_Success() throws CisException {
        String username = "testuser";
        String tokenParam = "dummyToken";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(statsService.getStats(Mockito.anyString(), Mockito.eq(username), Mockito.eq(authenticateResponseDto)))
                .thenReturn(mockResponse);

        ResponseEntity<GetStatsResponseDto> response = statsController.getStats(tokenParam, username, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void testGetStats_ServiceThrowsException() throws CisException {
        String username = "testuser";
        String tokenParam = "dummyToken";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(authenticateResponseDto);
        Mockito.when(statsService.getStats(Mockito.anyString(), Mockito.eq(username), Mockito.eq(authenticateResponseDto)))
                .thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Service Error"));



        CisException exception = assertThrows(CisException.class, () -> statsController.getStats(tokenParam, username, token));

        assertEquals("Service Error", exception.getMessage());
    }


    @Test
    void testGetStats_AuthResponseIsNull() throws CisException {
        String username = "testuser";
        String tokenParam = "dummyToken";

        Mockito.when(token.getAuthenticateResponseDto()).thenReturn(null);
        Mockito.when(statsService.getStats(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                .thenThrow(new NullPointerException("Null Auth Response"));

        assertThrows(NullPointerException.class, () -> statsController.getStats(tokenParam, username, token));
    }

    @Test
    void testGetStats_InvalidAuthenticationCast() {
        String username = "testuser";
        String tokenParam = "dummyToken";

        Authentication invalidAuth = Mockito.mock(Authentication.class);

        assertThrows(ClassCastException.class, () -> statsController.getStats(tokenParam, username, invalidAuth));
    }
}
