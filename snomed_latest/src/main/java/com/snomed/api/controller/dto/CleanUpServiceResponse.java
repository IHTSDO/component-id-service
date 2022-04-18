package com.snomed.api.controller.dto;

import org.springframework.stereotype.Component;

@Component
public class CleanUpServiceResponse {
    private Integer fieldCount;
    private Integer affectedRows;
    private Integer insertId;
    private Integer serverStatus;
    private Integer warningCount;
    private String message;
    private boolean protocol41;
    private Integer changedRows;
    private String model;

    public Integer getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }

    public Integer getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows = affectedRows;
    }

    public Integer getInsertId() {
        return insertId;
    }

    public void setInsertId(Integer insertId) {
        this.insertId = insertId;
    }

    public Integer getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(Integer serverStatus) {
        this.serverStatus = serverStatus;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isProtocol41() {
        return protocol41;
    }

    public void setProtocol41(boolean protocol41) {
        this.protocol41 = protocol41;
    }

    public Integer getChangedRows() {
        return changedRows;
    }

    public void setChangedRows(Integer changedRows) {
        this.changedRows = changedRows;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
