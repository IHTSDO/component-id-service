package org.snomed.cis.dto;

public class SchemeIdGenerateRequest {
 /*
    * {
"systemId": "string",
"software": "string",
"comment": "string"
}
    * */
    // requestbody change

    public String software;
    // public String expirationDate;
    public String comment;
    public String systemId;
    public String author;
    public boolean autoSysId;

    public SchemeIdGenerateRequest(String software, String comment, String systemId, String author, boolean autoSysId) {
        this.software = software;
        this.comment = comment;
        this.systemId = systemId;
        this.author = author;
        this.autoSysId = autoSysId;
    }

    public SchemeIdGenerateRequest() {
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

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
    }
}
