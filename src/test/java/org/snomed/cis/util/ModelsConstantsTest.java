package org.snomed.cis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelsConstantsTest {

    @Test
    void testConstantValues() {
        assertEquals("SchemeId", ModelsConstants.SCHEME_ID);
        assertEquals("SctId", ModelsConstants.SCTID);
        assertEquals("Available", ModelsConstants.AVAILABLE);
        assertEquals("Assigned", ModelsConstants.ASSIGNED);
        assertEquals("Reserved", ModelsConstants.RESERVED);
        assertEquals("Published", ModelsConstants.PUBLISHED);
        assertEquals("Deprecated", ModelsConstants.DEPRECATED);
    }

    @Test
    void testCanInstantiateModelsConstantsClass() {
        ModelsConstants instance = new ModelsConstants();
        assertNotNull(instance);
    }


}
