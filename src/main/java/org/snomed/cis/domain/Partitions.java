package org.snomed.cis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PartitionsPk.class)
@Table(name="partitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partitions implements Serializable {

    @Id
    private Integer namespace;

    @Id
    private String partitionId;

    private Integer sequence;

}
