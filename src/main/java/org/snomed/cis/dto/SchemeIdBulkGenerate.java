package org.snomed.cis.dto;

import org.snomed.cis.domain.SchemeName;

import java.util.Arrays;

//requestbody change
public class SchemeIdBulkGenerate {

    private Integer quantity;
    private String[] systemIds;

    private String software;
    private String comment;
    private boolean autoSysId;
    private  String author;
    private  String model;
    private SchemeName scheme;
    public String type;

    public SchemeIdBulkGenerate(Integer quantity, String[] systemIds, String software, String comment, boolean autoSysId, String author, String model, SchemeName scheme, String type) {
        this.quantity = quantity;
        this.systemIds = systemIds;
        this.software = software;
        this.comment = comment;
        this.autoSysId = autoSysId;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type = type;
    }

    public SchemeIdBulkGenerate() {
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String[] getSystemIds() {
        return systemIds;
    }

    public void setSystemIds(String[] systemIds) {
        this.systemIds = systemIds;
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

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
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

    public SchemeName getScheme() {
        return scheme;
    }

    public void setScheme(SchemeName scheme) {
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
                "quantity=" + quantity +
                ", systemIds size=" + (systemIds==null?"0":Arrays.toString(systemIds).length()) +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", autoSysId=" + autoSysId +
                ", author='" + author + '\'' +
                ", model='" + model + '\'' +
                ", scheme=" + scheme +
                ", type='" + type + '\'' +
                '}';
    }
}
