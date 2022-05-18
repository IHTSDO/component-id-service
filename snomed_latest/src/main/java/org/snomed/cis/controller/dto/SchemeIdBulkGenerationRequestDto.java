package org.snomed.cis.controller.dto;

import javax.validation.constraints.NotNull;

public class SchemeIdBulkGenerationRequestDto {

    /*
    * {
  "quantity": 0,
  "systemIds": [
    "string"
  ],
  "software": "string",
  "comment"
    * */

    //requestbody change
    @NotNull
    private Integer quantity;
    private String[] systemIds;

    private String software;
    private String comment;
   /* private boolean autoSysId;
    private  String author;
    private  String model;
    private SchemeName scheme;
    public String type;*/


    public SchemeIdBulkGenerationRequestDto() {
    }

    public SchemeIdBulkGenerationRequestDto(Integer quantity, String[] systemIds, String software, String comment) {
        this.quantity = quantity;
        this.systemIds = systemIds;
        this.software = software;
        this.comment = comment;

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

