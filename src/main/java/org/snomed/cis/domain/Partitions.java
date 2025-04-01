package org.snomed.cis.domain;

import lombok.*;

import jakarta.persistence.*;
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
    @Column(name = "namespace", columnDefinition = "INT")
    private Integer namespace;

    @Id
    @Column(name = "partitionId", columnDefinition = "VARCHAR(2)")
    private String partitionId;

    @Column(name = "sequence", columnDefinition = "INT")
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
