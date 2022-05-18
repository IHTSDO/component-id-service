package org.snomed.cis.controller.dto;

import javax.validation.constraints.NotNull;

public class SchemeIdBulkReserveRequestDto {
/*
* {
  "quantity": 0,
  "software": "string",
  "expirationDate": "string",
  "comment": "string"
}
* */

    //request body change
    @NotNull
    private Integer quantity;
    private String software;
    private String expirationDate;
    private String comment;


    public SchemeIdBulkReserveRequestDto(Integer quantity, String software, String expirationDate) {
        this.quantity = quantity;
        this.software = software;
        this.expirationDate = expirationDate;
        this.comment = comment;

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


}

