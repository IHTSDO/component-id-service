package org.snomed.cis.dto;

import org.snomed.cis.domain.Partitions;

import java.util.List;

public class NamespaceDto {

    private Integer namespace;
    private String organizationName;
    private String organizationAndContactDetails;
    private String dateIssued;
    private String email;
    private String notes;
    private String idPregenerate ;
    //private Namespace namespace;
    private List<Partitions> partitions;

    public NamespaceDto() {
    }

    public NamespaceDto(Integer namespace, String organizationName, String organizationAndContactDetails, String dateIssued, String email, String notes, String idPregenerate, List<Partitions> partitions) {
        this.namespace = namespace;
        this.organizationName = organizationName;
        this.organizationAndContactDetails = organizationAndContactDetails;
        this.dateIssued = dateIssued;
        this.email = email;
        this.notes = notes;
        this.idPregenerate = idPregenerate;
        this.partitions = partitions;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationAndContactDetails() {
        return organizationAndContactDetails;
    }

    public void setOrganizationAndContactDetails(String organizationAndContactDetails) {
        this.organizationAndContactDetails = organizationAndContactDetails;
    }

    public String getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<Partitions> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partitions> partitions) {
        this.partitions = partitions;
    }
}
