package com.snomed.api.controller.dto;

public class CheckSctidResponseDTO {
    private String sctid;
    private Long sequence;
    private Integer namespace;
    private String partitionID;
    private String componentType;
    private Integer checkDigit;
    private String isSCTIDValid;
    private String errorMessage;
    private String namespaceOrganization;
    private String namespaceContactEmail;

    public CheckSctidResponseDTO(String sctid, Long sequence, Integer namespace, String partitionID, String componentType, Integer checkDigit, String isSCTIDValid, String errorMessage, String namespaceOrganization, String namespaceContactEmail) {
        this.sctid = sctid;
        this.sequence = sequence;
        this.namespace = namespace;
        this.partitionID = partitionID;
        this.componentType = componentType;
        this.checkDigit = checkDigit;
        this.isSCTIDValid = isSCTIDValid;
        this.errorMessage = errorMessage;
        this.namespaceOrganization = namespaceOrganization;
        this.namespaceContactEmail = namespaceContactEmail;
    }

    public CheckSctidResponseDTO() {
    }

    public String getSctid() {
        return sctid;
    }

    public void setSctid(String sctid) {
        this.sctid = sctid;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    public String getPartitionID() {
        return partitionID;
    }

    public void setPartitionID(String partitionID) {
        this.partitionID = partitionID;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Integer getCheckDigit() {
        return checkDigit;
    }

    public void setCheckDigit(Integer checkDigit) {
        this.checkDigit = checkDigit;
    }

    public String getIsSCTIDValid() {
        return isSCTIDValid;
    }

    public void setIsSCTIDValid(String isSCTIDValid) {
        this.isSCTIDValid = isSCTIDValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNamespaceOrganization() {
        return namespaceOrganization;
    }

    public void setNamespaceOrganization(String namespaceOrganization) {
        this.namespaceOrganization = namespaceOrganization;
    }

    public String getNamespaceContactEmail() {
        return namespaceContactEmail;
    }

    public void setNamespaceContactEmail(String namespaceContactEmail) {
        this.namespaceContactEmail = namespaceContactEmail;
    }
}
