package com.snomed.api.domain;

public enum SchemeName {

    SNOMEDID("SNOMEDID"), CTV3ID("CTV3ID");

    public final String schemeName;

    public String toString() {
        return schemeName;
    }

    SchemeName(String schemeName) {
        this.schemeName = schemeName;
    }
}
