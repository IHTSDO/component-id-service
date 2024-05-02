package org.snomed.cis.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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
    private Integer namespace;

    @Id
    @NotNull
    private String username;

    @NotNull
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
