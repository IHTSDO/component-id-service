package com.snomed.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "sctid")
@Getter
@Setter
@NoArgsConstructor
public class Sctid {

    @Id
    @NotNull
    private String sctid;

    private long sequence;

    private Integer namespace;

    private String partitionId;

    private Integer checkDigit;

    @NotNull
    private String systemId;

    private String status;

    private String author;

    private String software;

    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    private String comment;

    @Nullable
    private Integer jobId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modified_at;

    public Sctid(String sctid, long sequence, Integer namespace, String partitionId, Integer checkDigit, String systemId, String status, String author, String software, Date expirationDate, String comment, Integer jobId, Date created_at, Date modified_at) {
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
    }

    @Override
    public String toString() {
        return "Sctid{" +
                "sctid='" + sctid + '\'' +
                ", sequence=" + sequence +
                ", namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", checkDigit=" + checkDigit +
                ", systemId='" + systemId + '\'' +
                ", status='" + status + '\'' +
                ", author='" + author + '\'' +
                ", software='" + software + '\'' +
                ", expirationDate=" + expirationDate +
                ", comment='" + comment + '\'' +
                ", jobId=" + jobId +
                ", created_at=" + created_at +
                ", modified_at=" + modified_at +
                '}';
    }
}