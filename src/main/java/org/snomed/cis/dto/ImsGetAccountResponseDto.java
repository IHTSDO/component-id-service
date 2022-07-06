package org.snomed.cis.dto;

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

    @Override
    public String toString() {
        return "{" +
                "login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", langKey='" + langKey + '\'' +
                ", roles size=" + (null==roles?"0": roles.size()) +
                '}';
    }
}
