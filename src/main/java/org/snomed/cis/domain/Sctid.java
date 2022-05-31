package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    private String comment;

    @Nullable
    private Integer jobId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

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