package org.snomed.cis.dto;

public class SchemeIdGenerateRequestDto {

    public String software;
    // public String expirationDate;
    public String comment;
    public String author;
    public String systemId;
    public boolean autoSysId;

    public SchemeIdGenerateRequestDto() {
    }

    public SchemeIdGenerateRequestDto(String software, String comment, String author, String systemId, boolean autoSysId) {
        this.software = software;
        this.comment = comment;
        this.author = author;
        this.systemId = systemId;
        this.autoSysId = autoSysId;
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

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
    }

}
