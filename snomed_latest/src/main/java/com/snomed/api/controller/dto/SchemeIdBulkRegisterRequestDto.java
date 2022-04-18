package com.snomed.api.controller.dto;

import java.util.List;

public class SchemeIdBulkRegisterRequestDto {

    private List<SchemeRegistrationRecord> records;
    private String software;
    private String comments;
    private String author;
    private String model;
    private String scheme;
    private String type;

    public SchemeIdBulkRegisterRequestDto() {
    }

    public SchemeIdBulkRegisterRequestDto(List<SchemeRegistrationRecord> records, String software, String comments, String author, String model, String scheme, String type) {
        this.records = records;
        this.software = software;
        this.comments = comments;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public List<SchemeRegistrationRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SchemeRegistrationRecord>  records) {
        this.records = records;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
