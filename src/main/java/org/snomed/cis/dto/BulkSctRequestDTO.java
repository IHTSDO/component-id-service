package org.snomed.cis.dto;

public class BulkSctRequestDTO {

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


    public BulkSctRequestDTO(String[] sctids, Integer namespace, String software, String comment) {
        this.sctids = sctids;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;
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

    @Override
    public String toString() {
        return "{" +
                "sctids=" + (null==sctids?"0": sctids.length) +
                ", namespace=" + namespace +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
