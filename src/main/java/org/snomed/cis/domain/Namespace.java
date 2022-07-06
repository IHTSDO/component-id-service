package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateIssued;

    private String email;

    private String notes;

    private String idPregenerate;

    public Namespace(Integer namespace, String organizationName, String organizationAndContactDetails, LocalDateTime dateIssued, String email, String notes, String idPregenerate
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

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", organizationName='" + organizationName + '\'' +
                ", organizationAndContactDetails='" + organizationAndContactDetails + '\'' +
                ", dateIssued=" + dateIssued +
                ", email='" + email + '\'' +
                ", notes='" + notes + '\'' +
                ", idPregenerate='" + idPregenerate + '\'' +
                '}';
    }
}
