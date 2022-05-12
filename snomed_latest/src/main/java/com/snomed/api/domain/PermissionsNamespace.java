package com.snomed.api.domain;

import com.sun.istack.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="permissionsnamespace")
@IdClass(PermissionsNamespacePK.class)
public class PermissionsNamespace implements Serializable {
    @Id
    private Integer namespace;

    @Id
    @NotNull
    private String username;

    @NotNull
    private String role;

    public PermissionsNamespace() {
    }

    public PermissionsNamespace(Integer namespace, String username, String role) {
        this.namespace = namespace;
        this.username = username;
        this.role = role;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public PermissionsNamespace setNamespace(Integer namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public PermissionsNamespace setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getRole() {
        return role;
    }

    public PermissionsNamespace setRole(String role) {
        this.role = role;
        return this;
    }
}
