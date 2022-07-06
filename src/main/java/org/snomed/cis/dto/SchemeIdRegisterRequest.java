package org.snomed.cis.dto;

import java.time.LocalDateTime;

public class SchemeIdRegisterRequest {

    /*
*
* {
  "schemeId": "string",
  "systemId": "string",
  "software": "string",
  "comment": "string"
}*/
    //requestbody change

    public String schemeId ;
    public String systemId;
    public String  software;
    public String comment;
    public String author;
    private LocalDateTime expirationDate;
    public boolean autoSysId;

    public SchemeIdRegisterRequest(String schemeId, String systemId, String software, String comment, String author, LocalDateTime expirationDate, boolean autoSysId) {
        this.schemeId = schemeId;
        this.systemId = systemId;
        this.software = software;
        this.comment = comment;
        this.author = author;
        this.expirationDate = expirationDate;
        this.autoSysId = autoSysId;
    }

    public SchemeIdRegisterRequest() {
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
    }

    @Override
    public String toString() {
        return "{" +
                "schemeId='" + schemeId + '\'' +
                ", systemId='" + systemId + '\'' +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", author='" + author + '\'' +
                ", expirationDate=" + expirationDate +
                ", autoSysId=" + autoSysId +
                '}';
    }
}
