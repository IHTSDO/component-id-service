package com.snomed.api.controller.dto;

public class RegistrationRecordsDTO {
    private String sctid;
    private String systemId;

    public RegistrationRecordsDTO(String sctid, String systemId) {
        this.sctid = sctid;
        this.systemId = systemId;
    }

    public String getSctid() {
        return sctid;
    }

    public void setSctid(String sctid) {
        this.sctid = sctid;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}
