package com.snomed.api.domain;

import com.sun.istack.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="permissionsnamespace")
public class PermissionsNamespace {
    @Id
    private int namespace=0;

    @NotNull
    private String username;

    @NotNull
    private String role;

    public int getNamespace() {
        return namespace;
    }

    public PermissionsNamespace setNamespace(int namespace) {
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
