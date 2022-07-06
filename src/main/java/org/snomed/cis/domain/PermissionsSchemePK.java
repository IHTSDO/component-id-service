package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PermissionsSchemePK implements Serializable {

    protected String scheme;

    protected String username;

    @Override
    public String toString() {
        return "{" +
                "scheme='" + scheme + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
