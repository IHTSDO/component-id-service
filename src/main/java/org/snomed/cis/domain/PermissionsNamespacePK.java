package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsNamespacePK implements Serializable {

    public Integer namespace;

    public String username;

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", username='" + username + '\'' +
                '}';
    }
}
