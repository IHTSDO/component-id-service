package org.snomed.cis.controller.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class LogoutRequestDto {

    @NotEmpty
    private String token;

}
