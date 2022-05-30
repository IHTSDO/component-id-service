package org.snomed.cis.repository;

import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BulkSchemeIdRepository extends JpaRepository<SchemeId, SchemeIdKey> {

    List<SchemeId> findBySchemeAndSchemeIdIn(String scheme, List<String> schemeId);

    Optional<SchemeId> findBySchemeAndSchemeId(String scheme, String schemeId);

    List<SchemeId> findBySchemeAndSystemId(String schemeName, String systemid);
}
