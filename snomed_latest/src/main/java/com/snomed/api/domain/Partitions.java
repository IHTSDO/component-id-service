package com.snomed.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
