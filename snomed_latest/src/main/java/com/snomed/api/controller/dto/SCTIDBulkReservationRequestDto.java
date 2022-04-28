package com.snomed.api.controller.dto;

import javax.validation.constraints.NotNull;

public class SCTIDBulkReservationRequestDto {
    /*
           * {
     "namespace": 0,
     "partitionId": "string",
     "expirationDate": "string",
     "quantity": 0,
     "software": "string",
     "comment": "string"
   }
           * */
    //requestbody chNGE
    @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    private String expirationDate;
    @NotNull
    private Integer quantity;
    private String software;
    private String comment;


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

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
                '}';
    }
}
