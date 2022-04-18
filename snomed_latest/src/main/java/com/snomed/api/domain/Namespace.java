package com.snomed.api.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="namespace")
public class Namespace {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer namespace;
    private String organizationName;
    private String organizationAndContactDetails;
    private Date dateIssued;
    private String email;
    private String notes;

    //private String idPregenerate ;

    //@OneToMany(mappedBy = "namespace")
    //private List<Partitions> partitionsList;

    public Namespace() {

    }

    public Namespace(Integer namespace, String organizationName, String organizationAndContactDetails, Date dateIssued, String email, String notes
                     //String idPregenerate
    ) {
        this.namespace = namespace;
        this.organizationName = organizationName;
        this.organizationAndContactDetails = organizationAndContactDetails;
        this.dateIssued = dateIssued;
        this.email = email;
        this.notes = notes;
        //this.idPregenerate = idPregenerate;
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

    /*public String getIdPregenerate() {
        return idPregenerate;
    }

    public void setIdPregenerate(String idPregenerate) {
        this.idPregenerate = idPregenerate;
    }*/

}
