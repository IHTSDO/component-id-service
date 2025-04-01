package org.snomed.cis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT")
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "status", columnDefinition = "VARCHAR(1)")
    private String status;

    @Column(name = "request", columnDefinition = "LONGTEXT")
    private String request;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "modified_at", columnDefinition = "DATETIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified_at = LocalDateTime.now();

    @Column(name = "log", columnDefinition = "VARCHAR(1000)")
    private String log;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", request='" + request + '\'' +
                ", created_at=" + created_at +
                ", modified_at=" + modified_at +
                ", log='" + log + '\'' +
                '}';
    }
}

