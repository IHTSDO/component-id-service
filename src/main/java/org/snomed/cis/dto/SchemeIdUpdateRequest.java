package org.snomed.cis.dto;

public class SchemeIdUpdateRequest {

    //requestbody change
    private String schemeId;
    private String software;
    private String comment;
    private String author;

    public SchemeIdUpdateRequest(String schemeId, String software, String comment, String author) {
        this.schemeId = schemeId;
        this.software = software;
        this.comment = comment;
        this.author = author;
    }

    public SchemeIdUpdateRequest() {
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
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

    @Override
    public String toString() {
        return "{" +
                "schemeId='" + schemeId + '\'' +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
