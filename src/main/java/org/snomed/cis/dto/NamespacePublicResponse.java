package org.snomed.cis.dto;

import java.util.Date;

public class NamespacePublicResponse {
    private Integer namespace;
    private String organizationName;
    private String organizationAndContactDetails;
    private Date dateIssued;
    private String email;
    private String notes;

    public NamespacePublicResponse() {
    }

    public NamespacePublicResponse(Integer namespace, String organizationName, String organizationAndContactDetails, Date dateIssued, String email, String notes) {
        this.namespace = namespace;
        this.organizationName = organizationName;
        this.organizationAndContactDetails = organizationAndContactDetails;
        this.dateIssued = dateIssued;
        this.email = email;
        this.notes = notes;
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

    public Date getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(Date dateIssued) {
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

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", organizationName='" + organizationName + '\'' +
                ", organizationAndContactDetails='" + organizationAndContactDetails + '\'' +
                ", dateIssued=" + dateIssued +
                ", email='" + email + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
