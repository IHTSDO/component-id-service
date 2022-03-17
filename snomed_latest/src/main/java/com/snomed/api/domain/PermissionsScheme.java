package com.snomed.api.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="permissionsscheme")
@IdClass(PermissionsSchemePK.class)
public class PermissionsScheme implements Serializable {

    @Id
    @NotNull
    private String scheme;

    @Id
    @NotNull
    private String username;
    private String role;

    public PermissionsScheme(String scheme, String username, String role) {
        this.scheme = scheme;
        this.username = username;
        this.role = role;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
