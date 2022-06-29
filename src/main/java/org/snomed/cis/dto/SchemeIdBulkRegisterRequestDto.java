package org.snomed.cis.dto;

import java.util.List;

public class SchemeIdBulkRegisterRequestDto {
/*
*
* {
  "records": [
    {
      "schemeId": "string",
      "systemId": "string"
    }
  ],
  "software": "string",
  "comment": "string"
}
* */
//requestbody change

    private List<SchemeRegistrationRecord> records;
    private String software;
    private String comment;


    public SchemeIdBulkRegisterRequestDto() {
    }

    public SchemeIdBulkRegisterRequestDto(List<SchemeRegistrationRecord> records, String software, String comments, String author, String model, String scheme, String type) {
        this.records = records;
        this.software = software;
        this.comment = comments;
    }



    public List<SchemeRegistrationRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SchemeRegistrationRecord>  records) {
        this.records = records;
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
