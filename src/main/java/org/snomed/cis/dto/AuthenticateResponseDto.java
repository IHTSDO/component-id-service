package org.snomed.cis.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AuthenticateResponseDto {

    private String name;

    @JsonProperty("first-name")
    private String firstName;

    @JsonProperty("last-name")
    private String lastName;

    @JsonProperty("display-name")
    private String displayName;

    private String email;

    @JsonIgnore
    private String langKey;

    @JsonIgnore
    private List<String> roles;

    @Override
    public String toString() {
        return "{" +
                "displayName='" + displayName + '\'' +
                '}';
    }
}
