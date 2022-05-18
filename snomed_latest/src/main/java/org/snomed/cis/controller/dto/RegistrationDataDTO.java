package org.snomed.cis.controller.dto;

import javax.validation.constraints.NotNull;

public class RegistrationDataDTO {

    /*
    * {
  "records": [
    {
      "sctid": "string",
      "systemId": "string"
    }
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}
* */
    //requestbody change
    @NotNull
    private RegistrationRecordsDTO[] records;
    @NotNull
    private Integer namespace;
    @NotNull
    private  String software;
    private String comment;


    public RegistrationDataDTO(@NotNull RegistrationRecordsDTO[] records, Integer namespace, String software, String comment) {
        this.records = records;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;

    }

    public RegistrationRecordsDTO[] getRecords() {
        return records;
    }

    public void setRecords(RegistrationRecordsDTO[] records) {
        this.records = records;
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


}
