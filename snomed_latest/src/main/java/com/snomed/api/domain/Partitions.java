package com.snomed.api.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PartitionsPk.class)
@Table(name="partitions")
public class Partitions implements Serializable {

    @Id
    //@ManyToOne
    //@JoinColumn(name="namespace", nullable = false)
    public Integer namespace;

    @Id
    public String partitionId;

    public Integer sequence;

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
