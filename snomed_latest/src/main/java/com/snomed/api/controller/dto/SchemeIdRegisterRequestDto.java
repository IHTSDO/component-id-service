package com.snomed.api.controller.dto;

import java.util.Date;

public class SchemeIdRegisterRequestDto {

    public String schemeId ;
    public String systemId;
    public String  software;
    public String comment;
    public String author;
    private Date expirationDate;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public SchemeIdRegisterRequestDto(String schemeId, String systemId, String software, String comment, boolean autoSysId,String author,Date expirationDate) {
        this.schemeId = schemeId;
        this.systemId = systemId;
        this.software = software;
        this.comment = comment;
        this.autoSysId = autoSysId;
        this.author=author;
        this.expirationDate=expirationDate;
    }

    public SchemeIdRegisterRequestDto() {
    }

    public boolean autoSysId;

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
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
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
