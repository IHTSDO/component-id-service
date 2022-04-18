package com.snomed.api.domain;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

public class SchemeIdPK implements Serializable {
    private String scheme;
    private String schemeId;
   /* private long sequence;
    private Integer checkDigit;
    private String systemId;
    private String status;
    private String author;
    private String software;
    private Date expirationDate;
    private String comment;
    private int jobId;
    private Date created_at;
    private Date modified_at;*/

    public SchemeIdPK(String scheme, String schemeId) {
        this.scheme = scheme;
        this.schemeId = schemeId;
    }

    public SchemeIdPK() {
    }
}
