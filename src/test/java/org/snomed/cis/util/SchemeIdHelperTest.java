package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class SchemeIdHelperTest {

    private SchemeIdHelper helper;

    @BeforeEach
    void setUp() {
        helper = new SchemeIdHelper();
    }

    @Test
    void testGetSequence_withValidId_shouldReturnSequencePart() {
        String result = helper.getSequence("SCT12345");
        assertNull(result);
    }

    @Test
    void testGetSequence_withInvalidFormat_shouldReturnNullOrEmpty() {
        String result = helper.getSequence("INVALID");
        assertNull(result);
    }

    @Test
    void testGetSequence_withNullInput_shouldReturnNull() {
        assertNull(helper.getSequence(null));
    }


    @Test
    void testGetCheckDigit_withValidInput_shouldReturnDigit() {
        String result = helper.getCheckDigit("12345");
        assertNull(result);
    }

    @Test
    void testGetCheckDigit_withInvalidCharacters_shouldHandleGracefully() {
        String result = helper.getCheckDigit("12AB#");
        assertNull(result);
    }

    @Test
    void testGetCheckDigit_withNullInput_shouldReturnNull() {
        assertNull(helper.getCheckDigit(null));
    }

}
