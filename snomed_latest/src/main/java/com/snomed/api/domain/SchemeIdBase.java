package com.snomed.api.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
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
