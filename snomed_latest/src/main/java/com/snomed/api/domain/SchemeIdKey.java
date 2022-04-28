package com.snomed.api.domain;

import java.io.Serializable;
import java.util.Date;

public class SchemeIdKey implements Serializable {

    private String scheme;
    private String schemeId;

    public SchemeIdKey() {
    }

    public SchemeIdKey(String scheme, String schemeId) {
        this.scheme = scheme;
        this.schemeId = schemeId;

    }
}
