package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "namespace")
@Getter
@Setter
@NoArgsConstructor
public class Namespace {

    @Id
    @Column(name = "namespace", columnDefinition = "INT")
    private Integer namespace;

    @Column(name = "organizationName", columnDefinition = "VARCHAR(255)")
    private String organizationName;

    @Column(name = "dateIssued", columnDefinition = "DATETIME")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateIssued;

    @Column(name = "email", columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(name = "notes", columnDefinition = "VARCHAR(2000)")
    private String notes;

    @Column(name = "idPregenerate", columnDefinition = "VARCHAR(1)")
    private String idPregenerate;

    public Namespace(Integer namespace, String organizationName, LocalDateTime dateIssued, String email, String notes, String idPregenerate
    ) {
        this.namespace = namespace;
        this.organizationName = organizationName;
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
                ", dateIssued=" + dateIssued +
                ", email='" + email + '\'' +
                ", notes='" + notes + '\'' +
                ", idPregenerate='" + idPregenerate + '\'' +
                '}';
    }
}
