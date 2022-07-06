package org.snomed.cis.dto;

public class DeprecateSctRequestDTO {

    /*
    * {
  "sctid": "string",
  "namespace": 0,
  "software": "string",
  "comment": "string"
}
    * */
    // request body change
    private String sctid;
    private Integer namespace;
    private String software;
    private String comment;


    public DeprecateSctRequestDTO() {
    }

    public DeprecateSctRequestDTO(String sctid, Integer namespace, String software, String comment) {
        this.sctid = sctid;
        this.namespace = namespace;
        this.software = software;
        this.comment = comment;

    }

    public String getSctid() {
        return sctid;
    }

    public void setSctid(String sctid) {
        this.sctid = sctid;
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
                "sctid='" + sctid + '\'' +
                ", namespace=" + namespace +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
