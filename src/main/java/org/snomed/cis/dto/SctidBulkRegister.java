package org.snomed.cis.dto;

import java.util.Arrays;

public class SctidBulkRegister {

    private RegistrationRecordsDTO[] records;

    private Integer namespace;

    private  String software;
    private String comment;
    private String model;
    private String author;
    private String type;

    public SctidBulkRegister(RegistrationRecordsDTO[] records, Integer namespace, String software, String comment, String model, String author, String type) {
        this.records = records;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;
        this.model = model;
        this.author = author;
        this.type = type;
    }

    public SctidBulkRegister() {
    }

    public RegistrationRecordsDTO[] getRecords() {
        return records;
    }

    public void setRecords(RegistrationRecordsDTO[] records) {
        this.records = records;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
                "records size=" + (records==null?"0":Arrays.toString(records).length()) +
                ", namespace=" + namespace +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", model='" + model + '\'' +
                ", author='" + author + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
