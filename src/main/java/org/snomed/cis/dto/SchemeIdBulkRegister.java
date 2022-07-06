package org.snomed.cis.dto;

import java.util.List;

public class SchemeIdBulkRegister {

    private List<SchemeRegistrationRecord> records;
    private String software;
    private String comment;

    private String author;
    private String model;
    private String scheme;
    private String type;

    public SchemeIdBulkRegister(List<SchemeRegistrationRecord> records, String software, String comment, String author, String model, String scheme, String type) {
        this.records = records;
        this.software = software;
        this.comment = comment;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type = type;
    }

    public SchemeIdBulkRegister() {
    }

    public List<SchemeRegistrationRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SchemeRegistrationRecord> records) {
        this.records = records;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "records size=" + (null==records?"0":records.size()) +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", author='" + author + '\'' +
                ", model='" + model + '\'' +
                ", scheme='" + scheme + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
