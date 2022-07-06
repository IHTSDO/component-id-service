package org.snomed.cis.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PartitionsPk.class)
@Table(name="partitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partitions implements Serializable {

    @Id
    private Integer namespace;

    @Id
    private String partitionId;

    private Integer sequence;

    @Override
    public String toString() {
        return "{" +
                "namespace=" + namespace +
                ", partitionId='" + partitionId + '\'' +
                ", sequence=" + sequence +
                '}';
    }
}
