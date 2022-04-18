package com.snomed.api.controller.dto;

import com.snomed.api.domain.SchemeName;

import javax.validation.constraints.NotNull;

public class SchemeIdBulkGenerationRequestDto {

    @NotNull
    private Integer quantity;
    private String[] systemIds;

    private String software;
    private String comment;
    private boolean autoSysId;
    private  String author;
    private  String model;
    private SchemeName scheme;
    public String type;


    public SchemeIdBulkGenerationRequestDto() {
    }

    public SchemeIdBulkGenerationRequestDto(Integer quantity, String[] systemIds, String software, String comment, boolean autoSysId, String author, String model, SchemeName scheme,String type) {
        this.quantity = quantity;
        this.systemIds = systemIds;
        this.software = software;
        this.comment = comment;
        this.autoSysId = autoSysId;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getSoftware()
    {
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

