package com.snomed.api.controller.dto;

public class DeprecateSctRequestDTO {
    private String sctid;
    private Integer namespace;
    private String software;
    private String comment;
    private String author;

    public DeprecateSctRequestDTO() {
    }

    public DeprecateSctRequestDTO(String sctid, Integer namespace, String software, String comment, String author) {
        this.sctid = sctid;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;
        this.author = author;
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
}
