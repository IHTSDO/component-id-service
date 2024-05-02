package org.snomed.cis.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class LogoutRequestDto {

    @NotEmpty
    private String token;

    @Override
    public String toString() {
        return "{" +
                "token='" + token + '\'' +
                '}';
    }
}
