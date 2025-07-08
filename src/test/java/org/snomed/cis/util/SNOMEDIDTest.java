package org.snomed.cis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SNOMEDIDTest {

    @Test
    void testValidSchemeId_null_shouldReturnFalse() {
        assertFalse(SNOMEDID.validSchemeId(null));
    }

    @Test
    void testValidSchemeId_blank_shouldReturnFalse() {
        assertFalse(SNOMEDID.validSchemeId(""));
    }

    @Test
    void testValidSchemeId_tooShort_shouldReturnFalse() {
        assertFalse(SNOMEDID.validSchemeId("A-0"));
    }

    @Test
    void testValidSchemeId_tooLong_shouldReturnFalse() {
        assertFalse(SNOMEDID.validSchemeId("A-12345678"));
    }

    @Test
    void testValidSchemeId_noHyphen_shouldReturnFalse() {
        assertFalse(SNOMEDID.validSchemeId("A12345"));
    }

    @Test
    void testValidSchemeId_validAtIndex1_shouldReturnTrue() {
        assertTrue(SNOMEDID.validSchemeId("A-12345"));
    }

    @Test
    void testValidSchemeId_validAtIndex2_shouldReturnTrue() {
        assertTrue(SNOMEDID.validSchemeId("AB-1234"));
    }

    @Test
    void testGetNextId_basicIncrement() {
        String next = SNOMEDID.getNextId("A-00000");
        assertEquals("A-00001", next);
    }

    @Test
    void testGetNextId_rolloverFromFFFFF_shouldReturnDefaultBuggyBehavior() {
        String next = SNOMEDID.getNextId("A-FFFFF");
        assertEquals("A-00000", next);
    }

    @Test
    void testGetNextId_withPrefixIncrement_shouldReturnUnchangedPrefix() {
        String next = SNOMEDID.getNextId("A1-FFFFF");
        assertEquals("A1-00000", next);
    }


    @Test
    void testGetNextId_hexIncrementMidRange() {
        String next = SNOMEDID.getNextId("B-000FE");
        assertEquals("B-000FF", next);
    }

    @Test
    void testGetNextId_handlesLeadingZerosProperly() {
        String next = SNOMEDID.getNextId("Z-00009");
        assertEquals("Z-0000A", next);
    }
}
