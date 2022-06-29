package org.snomed.cis.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
