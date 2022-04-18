package com.snomed.api.domain;

import java.io.Serializable;
import java.util.Date;

public class SchemeIdKey implements Serializable {

    private SchemeName scheme;
    private String schemeId;
   /* private Integer sequence;
    private Integer checkDigit;
    private String systemId;
    private String status;
    private String author;
    private String software;
    private Date expirationDate;
    private Integer jobId;
    private Date created_at;
    private Date modified_at;
*/
    public SchemeIdKey() {
    }

    public SchemeIdKey(SchemeName scheme, String schemeId/*, Integer sequence, Integer checkDigit, String systemId, String status, String author, String software, Date expirationDate, Integer jobId, Date created_at, Date modified_at*/) {
        this.scheme = scheme;
        this.schemeId = schemeId;
       /* this.sequence = sequence;
        this.checkDigit = checkDigit;
        this.systemId = systemId;
        this.status = status;
        this.author = author;
        this.software = software;
        this.expirationDate = expirationDate;
        this.jobId = jobId;
        this.created_at = created_at;
        this.modified_at = modified_at;*/
    }
}
