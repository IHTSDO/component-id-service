package org.snomed.cis.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {

    private String token;

    @Override
    public String toString() {
        return "{" +
                "token='" + token + '\'' +
                '}';
    }
}
