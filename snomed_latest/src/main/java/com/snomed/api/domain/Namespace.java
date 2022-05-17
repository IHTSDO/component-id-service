package com.snomed.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "namespace")
@Getter
@Setter
@NoArgsConstructor
public class Namespace {

    @Id
    private Integer namespace;

    private String organizationName;

    private String organizationAndContactDetails;

    @Column(name = "dateIssued")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateIssued;

    private String email;

    private String notes;

    private String idPregenerate;

    public Namespace(Integer namespace, String organizationName, String organizationAndContactDetails, Date dateIssued, String email, String notes, String idPregenerate
    ) {
        this.namespace = namespace;
        this.organizationName = organizationName;
        this.organizationAndContactDetails = organizationAndContactDetails;
        this.dateIssued = dateIssued;
        this.email = email;
        this.notes = notes;
        this.idPregenerate = idPregenerate;
    }

    public Namespace(Integer namespace) {
    }

}
