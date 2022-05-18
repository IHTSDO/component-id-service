package org.snomed.cis.controller.dto;

public class BulkSctRequest {


    /*
    * {
  "sctids": [
    "string"
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}*/
    //requestbody change
    private String[] sctids;
    private Integer namespace;
    private String software;
    private String comment;
    private String author;
    private String model;
    private String type;

    public BulkSctRequest(String[] sctids, Integer namespace, String software, String comment, String author, String model, String type) {
        this.sctids = sctids;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;
        this.author = author;
        this.model = model;
        this.type = type;
    }

    public BulkSctRequest() {
    }

    public String[] getSctids() {
        return sctids;
    }

    public void setSctids(String[] sctids) {
        this.sctids = sctids;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
