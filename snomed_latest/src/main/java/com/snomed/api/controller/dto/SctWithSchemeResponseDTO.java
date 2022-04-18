package com.snomed.api.controller.dto;

import com.snomed.api.domain.SchemeId;

import java.util.Date;
import java.util.List;

public class SctWithSchemeResponseDTO {
    private String sctid;
    private long sequence;
    private int namespace;
    private String partitionId;
    private Integer checkDigit;
    private String systemId;
    private String status;
    private String author;
    private String software;
    private Date expirationDate;
    private String comment;
    private List<SchemeId> additionalIds;

    public SctWithSchemeResponseDTO() {
    }

    public SctWithSchemeResponseDTO(String sctid, long sequence, int namespace, String partitionId, Integer checkDigit, String systemId, String status, String author, String software, Date expirationDate, String comment, List<SchemeId> additionalIds) {
        this.sctid = sctid;
        this.sequence = sequence;
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.checkDigit = checkDigit;
        this.systemId = systemId;
        this.status = status;
        this.author = author;
        this.software = software;
        this.expirationDate = expirationDate;
        this.comment = comment;
        this.additionalIds = additionalIds;
    }

    public String getSctid() {
        return sctid;
    }

    public void setSctid(String sctid) {
        this.sctid = sctid;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public int getNamespace() {
        return namespace;
    }

    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
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

    public List<SchemeId> getAdditionalIds() {
        return additionalIds;
    }

    public void setAdditionalIds(List<SchemeId> additionalIds) {
        this.additionalIds = additionalIds;
    }
}
