package org.snomed.cis.controller.dto;

import javax.validation.constraints.NotNull;

public class SCTIDRegisterRequest {

    //requestbody change
    private String sctid;
    private Integer namespace;
    private String systemId;
    private String software;
    private String comment;
    private boolean autoSysId = false;
    private String author;

    public SCTIDRegisterRequest(String sctid, Integer namespace, String systemId, String software, String comment, boolean autoSysId, String author) {
        this.sctid = sctid;
        this.namespace = namespace;
        this.systemId = systemId;
        this.software = software;
        this.comment = comment;
        this.autoSysId = autoSysId;
        this.author = author;
    }

    public SCTIDRegisterRequest() {
    }

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

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
