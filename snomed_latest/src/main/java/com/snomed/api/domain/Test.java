package com.snomed.api.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="testtbl")
public class Test {
    @Id
    private String testid;
    private String testName;

    public String getTestid() {
        return testid;
    }

    public void setTestid(String testid) {
        this.testid = testid;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    @Override
    public String toString() {
        return "Test{" +
                "testid='" + testid + '\'' +
                ", testName='" + testName + '\'' +
                '}';
    }

    public Test(String testid, String testName) {
        this.testid = testid;
        this.testName = testName;
    }

    protected Test(){}
}
