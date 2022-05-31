package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class PermissionsSchemePK implements Serializable {

    protected String scheme;

    protected String username;

    protected String role;

}
