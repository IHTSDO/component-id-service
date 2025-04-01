package org.snomed.cis.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "permissionsnamespace")
@IdClass(PermissionsNamespacePK.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsNamespace implements Serializable {

    @Id
    @Column(name = "namespace", columnDefinition = "INT")
    private Integer namespace;

    @Id
    @NotNull
    @Column(name = "username", columnDefinition = "VARCHAR(255)")
    private String username;

    @NotNull
    @Column(name = "role", columnDefinition = "VARCHAR(255)")
    private String role;

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
