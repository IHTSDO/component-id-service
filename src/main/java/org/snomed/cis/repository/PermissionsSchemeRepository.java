package org.snomed.cis.repository;

import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.PermissionsSchemePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface PermissionsSchemeRepository extends JpaRepository<PermissionsScheme, String> {

    List<PermissionsScheme> findByScheme(String scheme);

    List<PermissionsScheme> findByUsername(String username);

    @Modifying
    @Transactional
    void deleteBySchemeAndUsername(String scheme, String username);

    long countByUsernameIn(List<String> usernames);

    Optional<PermissionsScheme> findBySchemeAndUsernameAndRole(String scheme, String username, String role);

}
