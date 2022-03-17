package com.snomed.api.controller.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class SCTIDBulkGenerationRequestDto {

    public String scheme;
    @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    @NotNull
    private Integer quantity;
    private List<String> systemIds;
    private String software;
    private String comment;
    private String generateLegacyIds;
    public String model;
    public String author;
    public String type;
    private List<String> additionalJobs;

    public SCTIDBulkGenerationRequestDto() {
    }

    public String getType() {
        return type;
    }

    public SCTIDBulkGenerationRequestDto setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isAutoSysId() {
        return autoSysId;
    }

    public SCTIDBulkGenerationRequestDto setAutoSysId(boolean autoSysId) {
        this.autoSysId = autoSysId;
        return this;
    }

    private boolean autoSysId;

    public SCTIDBulkGenerationRequestDto(Integer namespace, String partitionId, Integer quantity, List<String> systemIds, String software, String comment, String generateLegacyIds) {
        this.namespace = namespace;
        this.partitionId = partitionId;
        this.quantity = quantity;
        this.systemIds = systemIds;
        this.software = software;
        this.comment = comment;
        this.generateLegacyIds = generateLegacyIds;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public SCTIDBulkGenerationRequestDto setNamespace(Integer namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public SCTIDBulkGenerationRequestDto setPartitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public SCTIDBulkGenerationRequestDto setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public List<String> getSystemIds() {
        return systemIds;
    }

    public SCTIDBulkGenerationRequestDto setSystemIds(List<String> systemIds) {
        this.systemIds = systemIds;
        return this;
    }

    public String getSoftware() {
        return software;
    }

    public SCTIDBulkGenerationRequestDto setSoftware(String software) {
        this.software = software;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public SCTIDBulkGenerationRequestDto setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getGenerateLegacyIds() {
        return generateLegacyIds;
    }

    public SCTIDBulkGenerationRequestDto setGenerateLegacyIds(String generateLegacyIds) {
        this.generateLegacyIds = generateLegacyIds;
        return this;
    }

    public String getModel() {
        return model;
    }

    public SCTIDBulkGenerationRequestDto setModel(String model) {
        this.model = model;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public SCTIDBulkGenerationRequestDto setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public SCTIDBulkGenerationRequestDto setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public List<String> getAdditionalJobs() {
        return additionalJobs;
    }

    public SCTIDBulkGenerationRequestDto setAdditionalJobs(List<String> additionalJobs) {
        this.additionalJobs = additionalJobs;
        return this;
    }

    public SCTIDBulkGenerationRequestDto copy(){

        SCTIDBulkGenerationRequestDto copyObj= new SCTIDBulkGenerationRequestDto();
        copyObj.setScheme(this.getScheme());
        copyObj.setNamespace(this.getNamespace());
        copyObj.setPartitionId(this.getPartitionId());
        copyObj.setQuantity(this.getQuantity());
        copyObj.setSystemIds(this.getSystemIds());
        copyObj.setSoftware(this.getSoftware());
        copyObj.setComment(this.getComment());
        copyObj.setGenerateLegacyIds(this.getGenerateLegacyIds());
        copyObj.setSystemIds(this.getSystemIds());
        copyObj.setAutoSysId(true);
        copyObj.setModel(this.getModel());
        copyObj.setAuthor(this.getAuthor());
        copyObj.setType(this.getType());
        return copyObj;
    }

    @Override
    public String
    toString() {
        return "SCTIDBulkGenerationRequestDto{" +
                "namespace='" + namespace + '\'' +
                ", partitionId='" + partitionId + '\'' +
                ", quantity=" + quantity +
                ", systemIds=" + systemIds +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", generateLegacyIds='" + generateLegacyIds + '\'' +
                ", model='" + model + '\'' +
                ", author='" + author + '\'' +
                ", autoSysId=" + autoSysId +
                '}';
    }
}
