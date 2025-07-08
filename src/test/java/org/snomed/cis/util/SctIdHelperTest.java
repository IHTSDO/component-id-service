package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.CheckSctidResponseDTO;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.SctidRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SctIdHelperTest {

    @InjectMocks
    private SctIdHelper sctIdHelper;

    @Mock
    private SctidRepository sctidRepository;

    @Mock
    private ModelsConstants modelsConstants;

    @Mock
    private NamespaceRepository namespaceRepository;

    @BeforeEach
    void init() {
        sctIdHelper = new SctIdHelper(namespaceRepository);
        sctIdHelper.sctidRepository = sctidRepository;
        sctIdHelper.modelsConstants = modelsConstants;
    }

    @Test
    void testValidSCTId_validInput_shouldReturnTrue() {
        String base = "1234567890";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String validSctid = base + checkDigit;
        assertTrue(SctIdHelper.validSCTId(validSctid));
    }


    @Test
    void testValidSCTId_invalidCheckDigit_shouldReturnFalse() {
        assertFalse(SctIdHelper.validSCTId("12345678901"));
    }

    @Test
    void testValidSCTId_null_shouldReturnFalse() {
        assertFalse(SctIdHelper.validSCTId(null));
    }

    @Test
    void testGetPartition_validId_shouldReturnPartition() {
        assertEquals("90", SctIdHelper.getPartition("12345678901"));
    }


    @Test
    void testGetPartition_shortId_shouldReturnNull() {
        assertNull(SctIdHelper.getPartition("12"));
    }

    @Test
    void testGetNamespace_coreId_shouldReturnZero() {
        String base = "1234567890";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String sctid = base + checkDigit;
        assertEquals(0, sctIdHelper.getNamespace(sctid));
    }

    @Test
    void testGetSctid_invalid_shouldThrowException() {
        assertThrows(CisException.class, () -> {
            sctIdHelper.getSctid("abc");
        });
    }

    @Test
    void testGetSctid_validNotFound_shouldReturnNew() throws CisException {
        String base = "1234567890";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String sctid = base + checkDigit;
        when(sctidRepository.findById(sctid)).thenReturn(Optional.empty());
        when(sctidRepository.save(any())).thenReturn(new Sctid());
        Sctid result = sctIdHelper.getSctid(sctid);
        assertNotNull(result);
    }

    @Test
    void testGetSequence_validExtension_shouldReturnSequence() {
        String base = "1234567890";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String sctid = base + checkDigit;
        assertNotNull(sctIdHelper.getSequence(sctid));
    }

    @Test
    void testGetSequence_extensionId_shouldReturnSequence() {
        String base = "9999999123";
        int checkDigit = SctIdHelper.verhoeffCompute(base + "891");
        String validId = base + "891" + checkDigit;
        Long sequence = sctIdHelper.getSequence(validId);
        assertNotNull(sequence);
    }

    @Test
    void testGetCheckDigit_validInput_shouldReturnDigit() {
        String base = "1234567890";
        int cd = SctIdHelper.verhoeffCompute(base);
        String sctid = base + cd;
        Integer actual = sctIdHelper.getCheckDigit(sctid);
        assertEquals(cd, actual);
    }

    @Test
    void testGetCheckDigit_invalidInput_shouldReturnNull() {
        assertNull(sctIdHelper.getCheckDigit("invalid"));
    }

    @Test
    void testGetNamespace_nullInput_shouldReturnNull() {
        assertNull(sctIdHelper.getNamespace(null));
    }

    @Test
    void testGetPartition_nullInput_shouldReturnNull() {
        assertNull(SctIdHelper.getPartition(null));
    }

    @Test
    void testValidSCTId_emptyString_shouldReturnFalse() {
        assertFalse(SctIdHelper.validSCTId(""));
    }

    @Test
    void testCheckSctid_tooShort_shouldReturnInvalid() throws CisException {
        CheckSctidResponseDTO dto = SctIdHelper.checkSctid("123");
        assertEquals("false", dto.getIsSCTIDValid());
    }

    @Test
    void testCheckSctid_tooLong_shouldReturnInvalid() throws CisException {
        CheckSctidResponseDTO dto = SctIdHelper.checkSctid("12345678901234567890123");
        assertEquals("false", dto.getIsSCTIDValid());
    }

    @Test
    void testCheckSctid_partitionInvalid_shouldReturnInvalid() throws CisException {
        String base = "9999999990";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String sctid = base + checkDigit;
        CheckSctidResponseDTO dto = SctIdHelper.checkSctid(sctid);
        assertEquals("false", dto.getIsSCTIDValid());
        assertTrue(dto.getErrorMessage().contains("Partition Id"));
    }

    @Test
    void testGetSctid_alreadyExists_shouldReturnExisting() throws CisException {
        String base = "1234567890";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String sctid = base + checkDigit;
        Sctid existing = new Sctid();
        when(sctidRepository.findById(sctid)).thenReturn(Optional.of(existing));
        Sctid result = sctIdHelper.getSctid(sctid);
        assertEquals(existing, result);
    }

}
