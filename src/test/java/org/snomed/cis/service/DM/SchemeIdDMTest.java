package org.snomed.cis.service.DM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.domain.SchemeId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SchemeIdDMTest {

    private SchemeIdDM schemeIdDM;
    private EntityManager entityManager;
    private Query mockQuery;

    @BeforeEach
    void setUp() {
        schemeIdDM = new SchemeIdDM();
        entityManager = mock(EntityManager.class);
        mockQuery = mock(Query.class);
        schemeIdDM.entityManager = entityManager;
    }

    @Test
    void testGetSchemeIds_withSystemIdAndPagination_shouldThrowClassCastException() {
        String systemId = "sys123";
        String limit = "10";
        String skip = "5";
        List<Object> invalidList = List.of("not-a-scheme-id");
        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn((List) invalidList);
        assertThrows(ClassCastException.class, () -> {
            schemeIdDM.getSchemeIds(systemId, limit, skip);
        });
    }

    @Test
    void testGetSchemeIds_forceMockToMatchCasting() {
        SchemeId dummy = new SchemeId();
        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenAnswer(invocation -> dummy); // Not a list
        assertThrows(ClassCastException.class, () -> {
            schemeIdDM.getSchemeIds("sys", "10", "0");
        });
    }


}
