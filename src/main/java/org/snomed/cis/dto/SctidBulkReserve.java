package org.snomed.cis.dto;

import jakarta.validation.constraints.NotNull;

public class SctidBulkReserve {
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
    private String model;
    private String author;
    public String type;

    public SctidBulkReserve(Integer namespace, String partitionId, String expirationDate, Integer quantity, String software, String comment, String model, String author, String type) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.software = software;
        this.comment = comment;
        this.model = model;
        this.author = author;
        this.type = type;
    }

    public SctidBulkReserve() {
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
                "namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", quantity=" + quantity +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", model='" + model + '\'' +
                ", author='" + author + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
