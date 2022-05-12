package com.snomed.api.domain;

import java.io.Serializable;

public class PermissionsNamespacePK implements Serializable {
    public Integer namespace;
    public String username;

    public PermissionsNamespacePK() {
    }

    public PermissionsNamespacePK(Integer namespace, String username) {
        this.namespace = namespace;
        this.username = username;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
