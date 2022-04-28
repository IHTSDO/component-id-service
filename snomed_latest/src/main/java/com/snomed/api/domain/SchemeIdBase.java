package com.snomed.api.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "schemeidbase")
public class SchemeIdBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private String scheme;

private String idBase;

    public SchemeIdBase(String scheme, String idBase) {
        this.scheme = scheme;
        this.idBase = idBase;
    }

    public SchemeIdBase() {
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getIdBase() {
        return idBase;
    }

    public void setIdBase(String idBase) {
        this.idBase = idBase;
    }
}
