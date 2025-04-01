package org.snomed.cis.domain;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "scheme", columnDefinition = "VARCHAR(160)")
    private String scheme;

    @Id
    @NotNull
    @Column(name = "username", columnDefinition = "VARCHAR(160)")
    private String username;

    @Column(name = "role", columnDefinition = "VARCHAR(255)")
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
