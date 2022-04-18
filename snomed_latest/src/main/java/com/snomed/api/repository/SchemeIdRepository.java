package com.snomed.api.repository;

import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeIdRepository extends JpaRepository<SchemeId, String> {
List<SchemeId> findBySchemeAndSystemId(SchemeName scheme, String systemId);
}
