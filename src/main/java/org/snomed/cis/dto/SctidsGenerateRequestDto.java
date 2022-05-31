package org.snomed.cis.dto;

public class SctidsGenerateRequestDto {
    private Integer namespace = 0;
    private String partitionId;
    private String systemId;
    private String software;
    private String comment;
    private boolean generateLegacyIds;

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

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isGenerateLegacyIds() {
        return generateLegacyIds;
    }

    public void setGenerateLegacyIds(boolean generateLegacyIds) {
        this.generateLegacyIds = generateLegacyIds;
    }
}
