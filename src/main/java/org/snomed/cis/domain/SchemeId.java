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
    private String scheme;

    @NotNull
    @Id
    private String schemeId;

    private Integer sequence;

    private Integer checkDigit;

    @NotNull
    private String systemId;

    private String status;

    private String author;

    private String software;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    private Integer jobId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified_at;

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
