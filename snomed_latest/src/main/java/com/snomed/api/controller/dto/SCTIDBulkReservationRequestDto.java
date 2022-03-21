package com.snomed.api.controller.dto;

import javax.validation.constraints.NotNull;

public class SCTIDBulkReservationRequestDto {

    @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    private String expirationDate;
    @NotNull
    private Integer quantity;
    private String software;
    private String comment;
    private String model;
    private String author;
    public String type;

    public SCTIDBulkReservationRequestDto() {
    }

    public SCTIDBulkReservationRequestDto(Integer namespace, String partitionId, String expirationDate, Integer quantity, String software, String comment) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.software = software;
        this.comment = comment;
    }



    public Integer getNamespace() {
        return namespace;
    }

    public SCTIDBulkReservationRequestDto setNamespace(Integer namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getModel() {
        return model;
    }

    public SCTIDBulkReservationRequestDto setModel(String model) {
        this.model = model;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public SCTIDBulkReservationRequestDto setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public SCTIDBulkReservationRequestDto setPartitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public SCTIDBulkReservationRequestDto setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public SCTIDBulkReservationRequestDto setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getSoftware() {
        return software;
    }

    public SCTIDBulkReservationRequestDto setSoftware(String software) {
        this.software = software;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public SCTIDBulkReservationRequestDto setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getType(){
        return type;
    }

    public void setType(String type)
    {
        this.type=type;
    }

    @Override
    public String toString() {
        return "SCTIDBulkReservationRequestDto{" +
                "namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", quantity=" + quantity +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", model='" + model + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
