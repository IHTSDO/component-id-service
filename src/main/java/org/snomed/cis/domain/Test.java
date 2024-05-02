package org.snomed.cis.domain;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "testtbl")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Test {
    @Id
    private String testid;
    private String testName;

    @Override
    public String toString() {
        return "Test{" +
                "testid='" + testid + '\'' +
                ", testName='" + testName + '\'' +
                '}';
    }

}
