package com.snomed.api.controller.dto;

import javax.validation.constraints.NotNull;

public class SCTIDReserveRequest {

   /*
        *
        * {
  "namespace": 0,
  "partitionId": "string",
  "expirationDate": "string",
  "software": "string",
  "comment": "string"
}*/

    //requestbody change

    private Integer namespace;

    private String partitionId;
    private String expirationDate;
    private String software;
    private String comment;
    private String author;

    public SCTIDReserveRequest(Integer namespace, String partitionId, String expirationDate, String software, String comment, String author) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.expirationDate = expirationDate;
        this.software = software;
        this.comment = comment;
        this.author = author;
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

    public SCTIDReserveRequest() {
    }
}

