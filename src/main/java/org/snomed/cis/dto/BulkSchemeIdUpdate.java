package org.snomed.cis.dto;

import java.util.List;
//requestbody change
public class BulkSchemeIdUpdate {

    private List<String> schemeIds;
    private String software;
    private String comment;
    private String author;
    private String model;
    private String scheme;
    private String type;

    public BulkSchemeIdUpdate(List<String> schemeIds, String software, String comment, String author, String model, String scheme, String type) {
        this.schemeIds = schemeIds;
        this.software = software;
        this.comment = comment;
        this.author = author;
        this.model = model;
        this.scheme = scheme;
        this.type = type;
    }

    public BulkSchemeIdUpdate() {
    }

    public List<String> getSchemeIds() {
        return schemeIds;
    }

    public void setSchemeIds(List<String> schemeIds) {
        this.schemeIds = schemeIds;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "schemeIds size=" + (null==schemeIds?"0":schemeIds.size()) +
                ", software='" + software + '\'' +
                ", comment='" + comment + '\'' +
                ", author='" + author + '\'' +
                ", model='" + model + '\'' +
                ", scheme='" + scheme + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
