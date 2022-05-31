package org.snomed.cis.controller.dto;

import org.snomed.cis.domain.BulkJob;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class BulkJobResponseDto {

    @Id
    private Integer id;
    private String name;
    private String author;
    private String Comment;
    private LocalDateTime ExpirationDate;
    private String status;
    private String request;
    private LocalDateTime created_at;
    private LocalDateTime requested_at;
    //private String log;
    private List<String> additionalJobs;
    public String type;
    public String systemId[];
    public int quantity;
    public Boolean autoSysId;
    private String partitionId;
    public Integer jobId;
    public Integer namespace;
    public  String software;
    public int sctid[];
    public String action;
    public String schemeId;
    public LocalDateTime modified_at;

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public LocalDateTime getModified_at() {
        return modified_at;
    }

    public void setModified_at(LocalDateTime modified_at) {
        this.modified_at = modified_at;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String scheme;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public RegistrationRecordsDTO[] getRecords() {
        return records;
    }

    public void setRecords(RegistrationRecordsDTO[] records) {
        this.records = records;
    }

    private RegistrationRecordsDTO[] records;

    public int[] getSctid() {
        return sctid;
    }

    public void setSctid(int [] sctid) {
        this.sctid = sctid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public LocalDateTime getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        ExpirationDate = expirationDate;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }



    public Boolean getAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(Boolean autoSysId) {
        this.autoSysId = autoSysId;
    }

    public String[] getSystemId() {
        return systemId;
    }

    public void setSystemId(String[] systemId) {
        this.systemId = systemId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BulkJobResponseDto(Integer id, String name, String status, String request, LocalDateTime created_at, LocalDateTime requested_at, String log, List<String> additionalJobs) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.request = request;
        this.created_at = created_at;
        this.requested_at = requested_at;
        //this.log = log;
        this.additionalJobs = additionalJobs;
    }

    public BulkJobResponseDto(BulkJob bulkJob) {
        this.id = bulkJob.getId();
        this.name = bulkJob.getName();
        this.status = bulkJob.getStatus();
        this.request = bulkJob.getRequest();
        this.created_at = bulkJob.getCreated_at();
        this.requested_at = bulkJob.getModified_at();
        //this.log = bulkJob.getLog();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getRequested_at() {
        return requested_at;
    }

    public void setRequested_at(LocalDateTime requested_at) {
        this.requested_at = requested_at;
    }

   /* public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }*/

    public List<String> getAdditionalJobs() {
        return additionalJobs;
    }

    public void setAdditionalJobs(List<String> additionalJobs) {
        this.additionalJobs = additionalJobs;
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


}
