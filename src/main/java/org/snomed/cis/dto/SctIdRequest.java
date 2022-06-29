package org.snomed.cis.dto;

public class SctIdRequest {
    private String sctids;

    public SctIdRequest() {
    }

    public SctIdRequest(String sctids) {
        this.sctids = sctids;
    }

    public String getSctids() {
        return sctids;
    }

    public void setSctids(String sctids) {
        this.sctids = sctids;
    }
}
