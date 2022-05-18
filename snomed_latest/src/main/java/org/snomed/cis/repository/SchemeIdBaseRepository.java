package org.snomed.cis.repository;

import org.snomed.cis.domain.SchemeIdBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemeIdBaseRepository extends JpaRepository<SchemeIdBase, String> {
    Optional<SchemeIdBase> findByScheme(String scheme);
}
