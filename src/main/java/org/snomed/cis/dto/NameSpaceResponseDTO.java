package org.snomed.cis.dto;

import java.time.LocalDateTime;

public class NameSpaceResponseDTO {



    private long namespace;
    private String organizationName;
    private LocalDateTime dateIssued;
    private String notes;
    private String idPregenerate;
    public long getNamespace() {
        return namespace;
    }

    public void setNamespace(long namespace) {
        this.namespace = namespace;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public LocalDateTime getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(LocalDateTime dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIdPregenerate() {
        return idPregenerate;
    }

    public void setIdPregenerate(String idPregenerate) {
        this.idPregenerate = idPregenerate;
    }
}
