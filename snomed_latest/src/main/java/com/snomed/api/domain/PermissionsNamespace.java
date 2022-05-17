package com.snomed.api.domain;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "permissionsnamespace")
@IdClass(PermissionsNamespacePK.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsNamespace implements Serializable {

    @Id
    private Integer namespace;

    @Id
    @NotNull
    private String username;

    @NotNull
    private String role;

}
