package org.snomed.cis.util;

import org.apache.commons.lang3.StringUtils;
import org.snomed.cis.dto.LoginRequestDto;
import org.snomed.cis.dto.SCTIDBulkGenerationRequestDto;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;

public class ValidationUtil {

    public static void validateLoginRequestDto(LoginRequestDto loginRequestDto) throws CisException {
        if (StringUtils.isEmpty(loginRequestDto.getUsername()))
            throw new CisException(HttpStatus.BAD_REQUEST, "username cannot be empty");
        if (StringUtils.isEmpty(loginRequestDto.getPassword()))
            throw new CisException(HttpStatus.BAD_REQUEST, "password cannot be empty");
    }

    public static void validateSctBulkGenerate(SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws CisException {
        if(sctidBulkGenerationRequestDto.getQuantity() <= 0)
            throw new CisException(HttpStatus.BAD_REQUEST, "quantity value must be positive number");
        if(sctidBulkGenerationRequestDto.getSystemIds() == null)
            throw new CisException(HttpStatus.BAD_REQUEST, "systemIds missing");
        if(sctidBulkGenerationRequestDto.getQuantity() != sctidBulkGenerationRequestDto.getSystemIds().size())
            throw new CisException(HttpStatus.BAD_REQUEST, "quantity does not match number of systemIds");
    }

}
