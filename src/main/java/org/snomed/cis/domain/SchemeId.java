package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "schemeid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(SchemeIdKey.class)
public class SchemeId implements Serializable {
    @Id
    @NotNull
    @Column(name = "scheme", columnDefinition = "VARCHAR(18)")
    private String scheme;

    @NotNull
    @Id
    @Column(name = "schemeId", columnDefinition = "VARCHAR(18)")
    private String schemeId;

    @Column(name = "sequence", columnDefinition = "INT")
    private Integer sequence;

    @Column(name = "checkDigit", columnDefinition = "INT")
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

    @Column(name = "expirationDate", columnDefinition = "DATETIME(6)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    @Column(name = "jobId", columnDefinition = "INT")
    private Integer jobId;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "modified_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified_at;

    @Column(name = "comment", columnDefinition = "VARCHAR(255)")
    private String comment;

    public SchemeId(String scheme, String schemeId, Integer sequence, Integer checkDigit, String systemId, String status, String author, String software, LocalDateTime expirationDate, Integer jobId, LocalDateTime created_at, LocalDateTime modified_at) {
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

    @Override
    public String toString() {
        return "{" +
                "scheme='" + scheme + '\'' +
                ", schemeId='" + schemeId + '\'' +
                ", sequence=" + sequence +
                ", checkDigit=" + checkDigit +
                ", systemId='" + systemId + '\'' +
                ", status='" + status + '\'' +
                ", author='" + author + '\'' +
                ", software='" + software + '\'' +
                ", expirationDate=" + expirationDate +
                ", jobId=" + jobId +
                ", created_at=" + created_at +
                ", modified_at=" + modified_at +
                ", comment='" + comment + '\'' +
                '}';
    }
}
