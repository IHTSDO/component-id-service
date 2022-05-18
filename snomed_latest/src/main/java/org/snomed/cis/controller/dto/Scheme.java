package org.snomed.cis.controller.dto;

public class Scheme {
    public String name;
    public String description;

    public Scheme() {
    }

    public Scheme(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
