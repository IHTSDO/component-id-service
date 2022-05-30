package org.snomed.cis.dto;

import java.util.List;

public class SchemeIdBulkDeprecateRequestDto {

    /*
    *
    * {
  "schemeIds": [
    "string"
  ],
  "software": "string",
  "comment": "string"
}*/
    private List<String> schemeIds;
    private String software;
    private String comment;


    public SchemeIdBulkDeprecateRequestDto() {

    }

    public SchemeIdBulkDeprecateRequestDto(List<String> schemeIds, String software, String comment) {
        this.schemeIds = schemeIds;
        this.software = software;
        this.comment = comment;

    }


    public List<String> getSchemeIds() {
        return schemeIds;
    }

    public void setSchemeIds(List<String> schemeIds) {
        this.schemeIds = schemeIds;
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
