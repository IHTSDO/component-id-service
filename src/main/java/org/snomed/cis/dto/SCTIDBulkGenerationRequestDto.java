package org.snomed.cis.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SCTIDBulkGenerationRequestDto {
    @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    @NotNull
    private Integer quantity;
    private List<String> systemIds;
    private String software;
    private String comment;
    private String generateLegacyIds="false";

    public SCTIDBulkGenerationRequestDto() {
    }




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

    public String getGenerateLegacyIds() {
        return generateLegacyIds;
    }

    public void setGenerateLegacyIds(String generateLegacyIds) {
        this.generateLegacyIds = generateLegacyIds;
    }

    public SCTIDBulkGenerationRequestDto copy(){
        SCTIDBulkGenerationRequestDto copyObj= new SCTIDBulkGenerationRequestDto();
        copyObj.setNamespace(this.getNamespace());
        copyObj.setPartitionId(this.getPartitionId());
        copyObj.setQuantity(this.getQuantity());
        copyObj.setSystemIds(this.getSystemIds());
        copyObj.setSoftware(this.getSoftware());
        copyObj.setComment(this.getComment());
        copyObj.setGenerateLegacyIds(this.getGenerateLegacyIds());
        return copyObj;
    }

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", quantity=" + quantity +
                ", systemIds size=" + (null==systemIds?"0": systemIds.size()) +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", generateLegacyIds='" + generateLegacyIds + '\'' +
                '}';
    }
}
