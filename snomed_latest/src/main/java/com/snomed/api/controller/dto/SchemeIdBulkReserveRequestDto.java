package com.snomed.api.controller.dto;

import javax.validation.constraints.NotNull;

public class SchemeIdBulkReserveRequestDto {

    @NotNull
    private Integer quantity;
    private String software;
    private String expirationDate;
    private String comment;
    private String author;
    private String model;
    private String scheme;
    private String type;

    public SchemeIdBulkReserveRequestDto(Integer quantity, String software, String expirationDate, String comment, String author, String model, String scheme, String type) {
        this.quantity = quantity;
        this.software = software;
        this.expirationDate = expirationDate;
        this.comment = comment;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type = type;
    }

    public SchemeIdBulkReserveRequestDto() {

    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
}

