package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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

}
