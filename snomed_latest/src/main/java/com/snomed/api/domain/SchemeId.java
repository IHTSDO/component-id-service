package com.snomed.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "schemeid")
@Getter
@Setter
@NoArgsConstructor
public class SchemeId implements Serializable {

    @NotNull
    private String scheme;

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String schemeId;

    private Integer sequence;

    private Integer checkDigit;

    @NotNull
    private String systemId;

    private String status;

    private String author;

    private String software;

    private Date expirationDate;

    private Integer jobId;

    private Date created_at;

    private Date modified_at;

    private String comment;

    public SchemeId(String scheme, String schemeId, Integer sequence, Integer checkDigit, String systemId, String status, String author, String software, Date expirationDate, Integer jobId, Date created_at, Date modified_at) {
        this.scheme = scheme;
        this.schemeId = schemeId;
        this.sequence = sequence;
        this.checkDigit = checkDigit;
        this.systemId = systemId;
        this.status = status;
        this.author = author;
        this.software = software;
        this.expirationDate = expirationDate;
        this.jobId = jobId;
        this.created_at = created_at;
        this.modified_at = modified_at;
    }

}
