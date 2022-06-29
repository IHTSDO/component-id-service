package org.snomed.cis.dto;

public class PartitionsDto {
    public Integer namespace;

    public String partitionId;
    public Integer sequence;

    public PartitionsDto(Integer namespaceId, String partitionId, Integer sequence) {
        this.namespace = namespaceId;
        this.partitionId = partitionId;
        this.sequence = sequence;
    }

    public PartitionsDto() {
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
