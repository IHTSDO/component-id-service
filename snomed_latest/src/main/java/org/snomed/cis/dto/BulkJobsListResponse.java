package org.snomed.cis.dto;

import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class BulkJobsListResponse {
    private Integer id;
    private String name;
    private String status;
    private Date created_at = new Date();
    private Date modified_at = new Date();

    public BulkJobsListResponse() {
    }

    public BulkJobsListResponse(Integer id, String name, String status, Date created_at, Date modified_at) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.created_at = created_at;
        this.modified_at = modified_at;
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
}
