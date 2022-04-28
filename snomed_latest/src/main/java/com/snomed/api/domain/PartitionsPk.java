package com.snomed.api.domain;

import java.io.Serializable;

public class PartitionsPk implements Serializable {
    public Integer namespace;
    public String partitionId;
   // public Integer sequence;

    public PartitionsPk() {
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

    public PartitionsPk(Integer namespace, String partitionId
                        //,Integer sequence
    ) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        //this.sequence = sequence;
    }
}
