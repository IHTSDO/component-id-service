package org.snomed.cis.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.snomed.cis.dto.LoginRequestDto;
import org.snomed.cis.exception.CisException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testValidLoginRequest_shouldPass() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        assertDoesNotThrow(() -> ValidationUtil.validateLoginRequestDto(dto));
    }

    static Stream<Arguments> invalidLoginInputs() {
        return Stream.of(org.junit.jupiter.params.provider.Arguments.of(null, "password", "username cannot be empty"), org.junit.jupiter.params.provider.Arguments.of("", "password", "username cannot be empty"), org.junit.jupiter.params.provider.Arguments.of("testuser", null, "password cannot be empty"), org.junit.jupiter.params.provider.Arguments.of("testuser", "", "password cannot be empty"));
    }

    @ParameterizedTest
    @MethodSource("invalidLoginInputs")
    void testInvalidLoginRequest(String username, String password, String expectedMessage) {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);

        CisException ex = assertThrows(CisException.class, () -> ValidationUtil.validateLoginRequestDto(dto));

        assertEquals(expectedMessage, ex.getMessage());
    }
}
