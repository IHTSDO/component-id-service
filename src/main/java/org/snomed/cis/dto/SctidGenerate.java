package org.snomed.cis.dto;
//requestbody change
public class SctidGenerate {
    private Integer namespace = 0;
    private String partitionId;
    private String systemId;
    private String software;
    private String comment;
    private boolean generateLegacyIds;
    private boolean autoSysId = false;
    private String author;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
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
