package org.snomed.cis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.snomed.cis.domain.SchemeId;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
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
    private LocalDateTime expirationDate;
    private String comment;
    @Nullable
    private Integer jobId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified_at;
    private List<SchemeId> additionalIds;

    public SctWithSchemeResponseDTO() {
    }

    public SctWithSchemeResponseDTO(String sctid, long sequence, int namespace, String partitionId, Integer checkDigit, String systemId, String status, String author, String software, LocalDateTime expirationDate, String comment, @Nullable Integer jobId, LocalDateTime created_at, LocalDateTime modified_at, List<SchemeId> additionalIds) {
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
        this.jobId = jobId;
        this.created_at = created_at;
        this.modified_at = modified_at;
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

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
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

    @Nullable
    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(@Nullable Integer jobId) {
        this.jobId = jobId;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getModified_at() {
        return modified_at;
    }

    public void setModified_at(LocalDateTime modified_at) {
        this.modified_at = modified_at;
    }

    public void setAdditionalIds(List<SchemeId> additionalIds) {
        this.additionalIds = additionalIds;
    }
}
