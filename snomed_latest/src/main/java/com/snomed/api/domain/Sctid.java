package com.snomed.api.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="sctid")
public class Sctid {
    @Id
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
    private int jobId;
    private Date created_at;
    private Date modified_at;
    /*@Column(name="check_digit")
    private int check;
    @Column(name="expiration_date")
    private Date expiration;*/

    protected Sctid(){}

    public Sctid(String sctid, long sequence, int namespace, String partitionId,
                 Integer checkDigit, String systemId, String status, String author,
                 String software, Date expirationDate, String comment, int jobId,
                 Date created_at, Date modified_at, int check,Date expiration) {
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
        //this.check = check;
        //this.expiration = expiration;
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

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
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

    /*public int getCheck() {
        return check;
    }

    public void setCheck(int check_digit) {
        this.check = check;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration_date(Date expiration_date) {
        this.expiration = expiration;
    }
*/
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
               // ", check=" + check +
                //", expiration=" + expiration +
                '}';
    }
}