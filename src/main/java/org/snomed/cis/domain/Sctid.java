package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.lang.Nullable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "sctid")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Sctid {

    @Id
    @NotNull
    @Column(name = "sctid", columnDefinition = "VARCHAR(18)")
    private String sctid;

    @Column(name = "sequence", columnDefinition = "BIGINT")
    private long sequence;

    @Column(name = "namespace", columnDefinition = "INT")
    private Integer namespace;

    @Column(name = "partitionId", columnDefinition = "VARCHAR(2)")
    private String partitionId;

    @Column(name = "checkDigit", columnDefinition = "TINYINT")
    private Integer checkDigit;

    @NotNull
    @Column(name = "systemId", columnDefinition = "VARCHAR(255)")
    private String systemId;

    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "author", columnDefinition = "VARCHAR(255)")
    private String author;

    @Column(name = "software", columnDefinition = "VARCHAR(255)")
    private String software;

    @Column(name = "expirationDate", columnDefinition = "DATE")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    @Column(name = "comment", columnDefinition = "VARCHAR(255)")
    private String comment;

    @Nullable
    @Column(name = "jobId", columnDefinition = "INT")
    private Integer jobId;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "modified_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified_at;

    public Sctid(String sctid, long sequence, Integer namespace, String partitionId, Integer checkDigit, String systemId, String status, String author, String software, LocalDateTime expirationDate, String comment, Integer jobId, LocalDateTime created_at, LocalDateTime modified_at) {
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
        return "{" +
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