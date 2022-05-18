package org.snomed.cis.controller.dto;

import javax.persistence.Id;
import java.util.Date;

public class SchemeIdResponseDTO {
    private String scheme;
    private String schemeId;
    private Long sequence;
    private Integer checkDigit;
    private String systemId;
    private String status;
    private String author;
    private String software;
    private Date expirationDate;
    private String comment;

    public SchemeIdResponseDTO() {
    }

    public SchemeIdResponseDTO(String scheme, String schemeId, Long sequence, Integer checkDigit, String systemId, String status, String author, String software, Date expirationDate, String comment) {
        this.scheme = scheme;
        this.schemeId = schemeId;
        this.sequence = sequence;
        this.checkDigit = checkDigit;
        this.systemId = systemId;
        this.status = status;
        this.author = author;
        this.software = software;
        this.expirationDate = expirationDate;
        this.comment = comment;
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

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
