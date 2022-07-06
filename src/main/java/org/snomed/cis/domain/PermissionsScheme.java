package org.snomed.cis.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
