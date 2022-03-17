package com.snomed.api.controller.dto;

import com.snomed.api.domain.BulkJob;

import javax.persistence.Id;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class BulkJobResponseDto {

    @Id
    private Integer id;
    private String name;
    private String status;
    private String request;
    private Date created_at = new Date();
    private Date requested_at = new Date();
    private String log;
    private List<String> additionalJobs;

    public BulkJobResponseDto(Integer id, String name, String status, String request, Date created_at, Date requested_at, String log, List<String> additionalJobs) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.request = request;
        this.created_at = created_at;
        this.requested_at = requested_at;
        this.log = log;
        this.additionalJobs = additionalJobs;
    }

    public BulkJobResponseDto(BulkJob bulkJob) {
        this.id = bulkJob.getId();
        this.name = bulkJob.getName();
        this.status = bulkJob.getStatus();
        this.request = bulkJob.getRequest();
        this.created_at = bulkJob.getCreated_at();
        this.requested_at = bulkJob.getRequested_at();
        this.log = bulkJob.getLog();
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

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getRequested_at() {
        return requested_at;
    }

    public void setRequested_at(Date requested_at) {
        this.requested_at = requested_at;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public List<String> getAdditionalJobs() {
        return additionalJobs;
    }

    public void setAdditionalJobs(List<String> additionalJobs) {
        this.additionalJobs = additionalJobs;
    }
}
