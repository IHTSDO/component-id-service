package com.snomed.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PartitionsPk.class)
@Table(name="partitions")
public class Partitions implements Serializable {


   /* @Id
    //@JoinColumn(name = "namespace")
    @ManyToOne
    @JoinColumn(name="namespace", nullable=false)
    //fix
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties("partitions")
    @JsonIgnore
    public Namespace namespace;*/

    @Id
    private Integer namespace;

    @Id
    private String partitionId;

    private Integer sequence;


    public Partitions() {
    }

    public Partitions(Integer namespace, String partitionId, Integer sequence) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.sequence = sequence;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}
