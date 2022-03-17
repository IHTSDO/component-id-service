package com.snomed.api.repository;

import com.snomed.api.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    @Query(
            value = "SELECT * FROM testtbl u",
            nativeQuery = true)
    List<Test> getAll();
}
