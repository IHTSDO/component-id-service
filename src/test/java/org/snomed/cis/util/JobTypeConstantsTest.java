package org.snomed.cis.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JobTypeConstantsTest {

    @Test
    void testAllJobTypeConstantsAreNotNullOrEmpty() {
        assertFalse(JobTypeConstants.GENERATE_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.REGISTER_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.RESERVE_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.DEPRECATE_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.RELEASE_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.PUBLISH_SCTIDS.isEmpty());
        assertFalse(JobTypeConstants.GENERATE_SCHEMEIDS.isEmpty());
        assertFalse(JobTypeConstants.REGISTER_SCHEMEIDS.isEmpty());
        assertFalse(JobTypeConstants.RESERVE_SCHEMEIDS.isEmpty());
        assertFalse(JobTypeConstants.DEPRECATE_SCHEMEIDS.isEmpty());
        assertFalse(JobTypeConstants.RELEASE_SCHEMEIDS.isEmpty());
        assertFalse(JobTypeConstants.PUBLISH_SCHEMEIDS.isEmpty());
    }

    @Test
    void testConstantValuesAreCorrect() {
        assertEquals("Generate SctIds", JobTypeConstants.GENERATE_SCTIDS);
        assertEquals("Register SctIds", JobTypeConstants.REGISTER_SCTIDS);
        assertEquals("Reserve SctIds", JobTypeConstants.RESERVE_SCTIDS);
        assertEquals("Deprecate SctIds", JobTypeConstants.DEPRECATE_SCTIDS);
        assertEquals("Release SctIds", JobTypeConstants.RELEASE_SCTIDS);
        assertEquals("Publish SctIds", JobTypeConstants.PUBLISH_SCTIDS);
        assertEquals("Generate SchemeIds", JobTypeConstants.GENERATE_SCHEMEIDS);
        assertEquals("Register SchemeIds", JobTypeConstants.REGISTER_SCHEMEIDS);
        assertEquals("Reserve SchemeIds", JobTypeConstants.RESERVE_SCHEMEIDS);
        assertEquals("Deprecate SchemeIds", JobTypeConstants.DEPRECATE_SCHEMEIDS);
        assertEquals("Release SchemeIds", JobTypeConstants.RELEASE_SCHEMEIDS);
        assertEquals("Publish SchemeIds", JobTypeConstants.PUBLISH_SCHEMEIDS);
    }

    @Test
    void testAllConstantValuesAreUnique() {
        var values = Set.of(JobTypeConstants.GENERATE_SCTIDS, JobTypeConstants.REGISTER_SCTIDS, JobTypeConstants.RESERVE_SCTIDS, JobTypeConstants.DEPRECATE_SCTIDS, JobTypeConstants.RELEASE_SCTIDS, JobTypeConstants.PUBLISH_SCTIDS, JobTypeConstants.GENERATE_SCHEMEIDS, JobTypeConstants.REGISTER_SCHEMEIDS, JobTypeConstants.RESERVE_SCHEMEIDS, JobTypeConstants.DEPRECATE_SCHEMEIDS, JobTypeConstants.RELEASE_SCHEMEIDS, JobTypeConstants.PUBLISH_SCHEMEIDS);

        assertEquals(12, values.size(), "Each constant value should be unique");
    }

    @Test
    void testAllFieldsAreStaticFinal() throws Exception {
        for (var field : JobTypeConstants.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isStatic(modifiers), field.getName() + " should be static");
            assertTrue(Modifier.isFinal(modifiers), field.getName() + " should be final");
        }
    }

    @Test
    void testCanInstantiateJobTypeConstantsClass() throws Exception {
        Constructor<JobTypeConstants> constructor = JobTypeConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        JobTypeConstants instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testJobTypeUsageInMap() {
        Map<String, String> jobMap = Map.of(JobTypeConstants.GENERATE_SCTIDS, "Handled", JobTypeConstants.RESERVE_SCTIDS, "Handled");
        assertEquals("Handled", jobMap.get(JobTypeConstants.GENERATE_SCTIDS));
    }

}
