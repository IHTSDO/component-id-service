package com.snomed.api.domain;

import java.io.Serializable;

public class PermissionsSchemePK implements Serializable {
    protected String scheme;
    protected String username;
    protected String role;

    public PermissionsSchemePK() {}

    public PermissionsSchemePK(String scheme, String username,String role) {
        this.scheme = scheme;
        this.username = username;
        this.role = role;
    }
}
