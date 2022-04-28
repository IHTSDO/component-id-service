package com.snomed.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PartitionsPk.class)
@Table(name="partitions")
public class Partitions implements Serializable {


    @Id
    //@JoinColumn(name = "namespace")
    @ManyToOne
    @JoinColumn(name="namespace", nullable=false)
    //fix
    @JsonIgnore
    public Namespace namespace;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String partitionId;

    public Partitions(Namespace namespaceId, String partitionId, Integer sequence) {
        this.namespace = namespaceId;
        this.partitionId = partitionId;
        this.sequence = sequence;
    }

    @Column
    public Integer sequence;


    public Partitions() {
    }

    public Partitions(Namespace namespace, Integer sequence) {
       // this.namespace = namespace;
        this.sequence = sequence;
    }



    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
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
