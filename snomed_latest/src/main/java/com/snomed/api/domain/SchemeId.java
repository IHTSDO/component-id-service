package com.snomed.api.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="schemeid")
/*
@IdClass(SchemeIdKey.class)
*/
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


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String comment;

    public SchemeId() {
    }

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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getCheckDigit() {
        return checkDigit;
    }

    public void setCheckDigit(Integer checkDigit) {
        this.checkDigit = checkDigit;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getModified_at() {
        return modified_at;
    }

    public void setModified_at(Date modified_at) {
        this.modified_at = modified_at;
    }
}
