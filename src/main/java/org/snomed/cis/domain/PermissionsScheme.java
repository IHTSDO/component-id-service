package org.snomed.cis.domain;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "permissionsscheme")
@IdClass(PermissionsSchemePK.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionsScheme implements Serializable {

    @Id
    @NotNull
    private String scheme;

    @Id
    @NotNull
    private String username;

    private String role;

    @Override
    public String toString() {
        return "{" +
                "scheme='" + scheme + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
