package org.snomed.cis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateRequestDto {

    @NotEmpty
    private String token;


    @Override
    public String toString() {
        return "{" +
                "token='" + token + '\'' +
                '}';
    }
}
