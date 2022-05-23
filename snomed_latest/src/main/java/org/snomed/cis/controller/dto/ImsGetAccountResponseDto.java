package org.snomed.cis.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImsGetAccountResponseDto {

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private String langKey;

    private List<String> roles;

}
