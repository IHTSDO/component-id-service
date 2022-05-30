package org.snomed.cis.dto;

import javax.validation.constraints.NotNull;

public class SCTIDReservationRequest {

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
    @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    private String expirationDate;
    private String software;
    private String comment;

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
}
