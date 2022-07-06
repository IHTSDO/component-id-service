package org.snomed.cis.dto;

import java.util.List;

public class SctidBulkGenerate {

    public String scheme;
    private Integer namespace;
    private String partitionId;
    private Integer quantity;
    private List<String> systemIds;
    private String software;
    private String comment;
    private String generateLegacyIds="false";
    public String model;
    public String author;
    public String type;
    private List<String> additionalJobs;
    private boolean autoSysId=false;

    public SctidBulkGenerate(String scheme, Integer namespace, String partitionId, Integer quantity, List<String> systemIds, String software, String comment, String generateLegacyIds, String model, String author, String type, List<String> additionalJobs, boolean autoSysId) {
        this.scheme = scheme;
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.quantity = quantity;
        this.systemIds = systemIds;
        this.software = software;
        this.comment = comment;
        this.generateLegacyIds = generateLegacyIds;
        this.model = model;
        this.author = author;
        this.type = type;
        this.additionalJobs = additionalJobs;
        this.autoSysId = autoSysId;
    }

    public SctidBulkGenerate() {
    }

    public String getGenerateLegacyIds() {
        return generateLegacyIds;
    }

    public void setGenerateLegacyIds(String generateLegacyIds) {
        this.generateLegacyIds = generateLegacyIds;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<String> getSystemIds() {
        return systemIds;
    }

    public void setSystemIds(List<String> systemIds) {
        this.systemIds = systemIds;
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


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAdditionalJobs() {
        return additionalJobs;
    }

    public void setAdditionalJobs(List<String> additionalJobs) {
        this.additionalJobs = additionalJobs;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public void setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
    }

    public SctidBulkGenerate copy(){

        SctidBulkGenerate copyObj= new SctidBulkGenerate();
        copyObj.setNamespace(this.getNamespace());
        copyObj.setPartitionId(this.getPartitionId());
        copyObj.setQuantity(this.getQuantity());
        copyObj.setSystemIds(this.getSystemIds());
        copyObj.setSoftware(this.getSoftware());
        copyObj.setComment(this.getComment());
        copyObj.setGenerateLegacyIds(this.getGenerateLegacyIds());
        copyObj.setModel(this.getModel());
        copyObj.setType(this.getType());
        copyObj.setAuthor(this.getAuthor());
        copyObj.setAdditionalJobs(this.getAdditionalJobs());
        copyObj.setAutoSysId(this.isAutoSysId());
        return copyObj;
    }

    @Override
    public String toString() {
        return "SctidBulkGenerate{" +
                "scheme='" + scheme + '\'' +
                ", namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", quantity=" + quantity +
                ", systemIds size=" + (null==systemIds?"0":systemIds.size()) +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", generateLegacyIds='" + generateLegacyIds + '\'' +
                ", model='" + model + '\'' +
                ", author='" + author + '\'' +
                ", type='" + type + '\'' +
                ", additionalJobs size=" + (null==additionalJobs?"0":additionalJobs.size()) +
                ", autoSysId=" + autoSysId +
                '}';
    }
}
