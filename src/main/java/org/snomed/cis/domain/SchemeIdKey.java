package org.snomed.cis.domain;

import java.io.Serializable;

public class SchemeIdKey implements Serializable {

    private String scheme;
    private String schemeId;

    public SchemeIdKey() {
    }

    public SchemeIdKey(String scheme, String schemeId) {
        this.scheme = scheme;
        this.schemeId = schemeId;
    }

    @Override
    public String toString() {
        return "{" +
                "scheme='" + scheme + '\'' +
                ", schemeId='" + schemeId + '\'' +
                '}';
    }
}
