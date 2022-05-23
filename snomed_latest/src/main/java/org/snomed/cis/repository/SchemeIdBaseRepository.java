package org.snomed.cis.repository;

import org.snomed.cis.domain.SchemeIdBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchemeIdBaseRepository extends JpaRepository<SchemeIdBase, String> {
    Optional<SchemeIdBase> findByScheme(String scheme);
}
