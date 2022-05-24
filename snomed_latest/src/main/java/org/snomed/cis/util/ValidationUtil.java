package org.snomed.cis.util;

import org.apache.commons.lang3.StringUtils;
import org.snomed.cis.controller.dto.LoginRequestDto;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;

public class ValidationUtil {

    public static void validateLoginRequestDto(LoginRequestDto loginRequestDto) throws CisException {
        if (StringUtils.isEmpty(loginRequestDto.getUsername()))
            throw new CisException(HttpStatus.BAD_REQUEST, "username cannot be empty");
        if (StringUtils.isEmpty(loginRequestDto.getPassword()))
            throw new CisException(HttpStatus.BAD_REQUEST, "password cannot be empty");
    }

}
