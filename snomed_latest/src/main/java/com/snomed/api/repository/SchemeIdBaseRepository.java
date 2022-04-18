package com.snomed.api.repository;

import com.snomed.api.domain.SchemeIdBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeIdBaseRepository extends JpaRepository<SchemeIdBase, String> {
    List<SchemeIdBase> findByScheme(String scheme);
}
