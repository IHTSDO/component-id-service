package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "schemeidbase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchemeIdBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private String scheme;

    private String idBase;

    @Override
    public String toString() {
        return "{" +
                "scheme='" + scheme + '\'' +
                ", idBase='" + idBase + '\'' +
                '}';
    }
}
