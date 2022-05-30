package org.snomed.cis.dto;

public class SchemeIdUpdateRequestDto {
    private String schemeId;
    private String software;
    private String comment;
    private String author;


    public SchemeIdUpdateRequestDto() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public SchemeIdUpdateRequestDto(String schemeId, String software, String comment, String author) {
        this.schemeId = schemeId;
        this.software = software;
        this.comment = comment;
        this.author=author;
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
}
