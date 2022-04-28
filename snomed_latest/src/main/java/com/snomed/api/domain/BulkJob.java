package com.snomed.api.domain;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;

@Entity

public class BulkJob  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    private String name;
    private String status;
    private String request;
    private Date created_at = new Date();
    private Date modified_at = new Date();
    private String log;

    public BulkJob() {
    }

    public BulkJob(Integer id, String name, String status, String request, Date created_at, Date modified_at, String log) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.request = request;
        this.created_at = created_at;
        this.modified_at = modified_at;
        this.log = log;
    }

    public Integer getId() {
        return id;
    }

    public BulkJob setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BulkJob setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public BulkJob setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getRequest() {
        return request;
    }

    public BulkJob setRequest(String request) {
        this.request = request;
        return this;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public BulkJob setCreated_at(Date created_at) {
        this.created_at = created_at;
        return this;
    }

    public Date getRequested_at() {
        return modified_at;
    }

    public BulkJob setRequested_at(Date modified_at) {
        this.modified_at = modified_at;
        return this;
    }

    public String getLog() {
        return log;
    }

    public BulkJob setLog(String log) {
        this.log = log;
        return this;
    }
}
