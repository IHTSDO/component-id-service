package org.snomed.cis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CTV3IDTest {

    @Test
    void testValidSchemeId_validInput_shouldReturnTrue() {
        assertTrue(CTV3ID.validSchemeId("A1b2Z"));
    }

    @Test
    void testValidSchemeId_withDots_shouldReturnTrue() {
        assertTrue(CTV3ID.validSchemeId("AB..C"));
    }

    @Test
    void testValidSchemeId_nullInput_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId(null));
    }

    @Test
    void testValidSchemeId_emptyString_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId(""));
    }

    @Test
    void testValidSchemeId_blankSpaces_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId("     "));
    }

    @Test
    void testValidSchemeId_invalidLength_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId("abc"));
        assertFalse(CTV3ID.validSchemeId("abcdef"));
    }

    @Test
    void testValidSchemeId_invalidCharacters_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId("1234$"));
        assertFalse(CTV3ID.validSchemeId("12_4A"));
    }

    @Test
    void testGetNextId_simpleIncrement() {
        assertEquals("A", CTV3ID.getNextId("9"));
    }

    @Test
    void testGetNextId_overflowShouldWrap() {
        assertEquals("10", CTV3ID.getNextId("z"));
    }

    @Test
    void testGetNextId_multipleDigitCarry() {
        assertEquals("100", CTV3ID.getNextId("zz"));
    }

    @Test
    void testGetNextId_allZeroes() {
        assertEquals("1", CTV3ID.getNextId("0"));
    }

    @Test
    void testGetNextId_randomAlphaNumeric() {
        String next = CTV3ID.getNextId("A1B2Z");
        assertNotNull(next);
        assertFalse(next.isEmpty());
    }

    @Test
    void testValidSchemeId_validAlphaNumWithDot_shouldReturnTrue() {
        assertTrue(CTV3ID.validSchemeId("a.Z9b"));
    }

    @Test
    void testValidSchemeId_dotAtStart_shouldReturnTrue() {
        assertTrue(CTV3ID.validSchemeId(".Z9bC"));
    }

    @Test
    void testValidSchemeId_dotAtEnd_shouldReturnTrue() {
        assertTrue(CTV3ID.validSchemeId("Z9bC."));
    }

    @Test
    void testValidSchemeId_containsSpecialCharacters_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId("A@#12"));
    }

    @Test
    void testValidSchemeId_containsWhitespace_shouldReturnFalse() {
        assertFalse(CTV3ID.validSchemeId("AB C1"));
    }

    @Test
    void testGetNextId_lowercaseId_shouldReturnNext() {
        assertEquals("b", CTV3ID.getNextId("a"));
    }

    @Test
    void testGetNextId_mixedCaseInput_shouldReturnNext() {
        String result = CTV3ID.getNextId("AzZ");
        assertNotNull(result);
    }

    @Test
    void testGetNextId_maxSingleChar_shouldReturnTwoChar() {
        assertEquals("10", CTV3ID.getNextId("z"));
    }

    @Test
    void testGetNextId_largeInputValue_shouldReturnValidNext() {
        String large = "zzzzz";
        String result = CTV3ID.getNextId(large);
        assertNotNull(result);
        assertTrue(result.length() >= 5);
    }

    @Test
    void testGetNextId_invalidCharacter_shouldReturnNegativeIndex() {
        String next = CTV3ID.getNextId("A$B1C");
        assertNotNull(next);
    }

}
