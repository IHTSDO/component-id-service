package org.snomed.cis.dto;

public class SchemeIdReserveRequestDto {

    public String software;
    public String expirationDate;
    public String comment;
    public String author;

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    private String systemId;

    public SchemeIdReserveRequestDto() {
    }

    public SchemeIdReserveRequestDto(String software, String expirationDate, String comment, String author,String systemId) {
        this.software = software;
        this.expirationDate = expirationDate;
        this.comment = comment;
        this.author = author;
        this.systemId=systemId;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
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

    @Override
    public String toString() {
        return "{" +
                "software='" + software + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", comment='" + comment + '\'' +
                ", author='" + author + '\'' +
                ", systemId='" + systemId + '\'' +
                '}';
    }
}
