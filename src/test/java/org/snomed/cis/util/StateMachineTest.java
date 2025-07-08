package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private StateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new StateMachine();
    }

    @Test
    void testAvailableState_withRegister_shouldReturnAssigned() {
        String result = stateMachine.getNewStatus("available", "Register");
        assertEquals("Assigned", result);
    }

    @Test
    void testAvailableState_withGenerate_shouldReturnAssigned() {
        String result = stateMachine.getNewStatus("available", "Generate");
        assertEquals("Assigned", result);
    }

    @Test
    void testAvailableState_withReserve_shouldReturnReserved() {
        String result = stateMachine.getNewStatus("available", "Reserve");
        assertEquals("Reserved", result);
    }

    @Test
    void testAssignedState_withDeprecate_shouldReturnDeprecated() {
        String result = stateMachine.getNewStatus("assigned", "Deprecate");
        assertEquals("Deprecated", result);
    }

    @Test
    void testAssignedState_withPublish_shouldReturnPublished() {
        String result = stateMachine.getNewStatus("assigned", "Publish");
        assertEquals("Published", result);
    }

    @Test
    void testAssignedState_withRelease_shouldReturnAvailable() {
        String result = stateMachine.getNewStatus("assigned", "Release");
        assertEquals("Available", result);
    }

    @Test
    void testReservedState_withRelease_shouldReturnAvailable() {
        String result = stateMachine.getNewStatus("reserved", "Release");
        assertEquals("Available", result);
    }

    @Test
    void testReservedState_withRegister_shouldReturnAssigned() {
        String result = stateMachine.getNewStatus("reserved", "Register");
        assertEquals("Assigned", result);
    }

    @Test
    void testPublishedState_withDeprecate_shouldReturnDeprecated() {
        String result = stateMachine.getNewStatus("published", "Deprecate");
        assertEquals("Deprecated", result);
    }

    @Test
    void testDeprecatedState_withPublish_shouldReturnPublished() {
        String result = stateMachine.getNewStatus("deprecated", "Publish");
        assertEquals("Published", result);
    }

    @Test
    void testInvalidAction_shouldReturnNull() {
        String result = stateMachine.getNewStatus("available", "Deactivate");
        assertNull(result);
    }

    @Test
    void testInvalidState_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            stateMachine.getNewStatus("unknown", "Register");
        });
    }


    @Test
    void testCaseInsensitiveStatus_shouldWork() {
        String result = stateMachine.getNewStatus("Available", "Register");
        assertEquals("Assigned", result);
    }

    @Test
    void testNullStatus_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            stateMachine.getNewStatus(null, "Register");
        });
    }

    @Test
    void testNullAction_shouldReturnNull() {
        String result = null;
        try {
            result = stateMachine.getNewStatus("available", null);
        } catch (Exception e) {
            result = null;
        }
        assertNull(result);
    }

}
