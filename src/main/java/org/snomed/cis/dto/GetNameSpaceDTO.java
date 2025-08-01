package org.snomed.cis.dto;

import org.snomed.cis.domain.Partitions;

import java.util.List;

public class GetNameSpaceDTO {
    private Integer namespace;
    private String organizationName;
    private String dateIssued;

    private String notes;
    private String idPregenerate ;
    private List<Partitions> partitions;

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


    public String getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(String dateIssued) {
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

    public List<Partitions> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partitions> partitions) {
        this.partitions = partitions;
    }


}
