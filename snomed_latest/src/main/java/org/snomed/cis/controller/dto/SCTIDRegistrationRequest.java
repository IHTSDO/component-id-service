package org.snomed.cis.controller.dto;

import javax.validation.constraints.NotNull;

public class SCTIDRegistrationRequest {


    /*
    *
    * {
  "sctid": "string",
  "namespace": 0,
  "systemId": "string",
  "software": "string",
  "comment": "string"
}*/
    //requestbody change
    @NotNull
    private String sctid;
    @NotNull
    private Integer namespace;
    private String systemId;
    private String software;
    private String comment;


    public String getSctid() {
        return sctid;
    }

    public void setSctid(String sctid) {
        this.sctid = sctid;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

   }
