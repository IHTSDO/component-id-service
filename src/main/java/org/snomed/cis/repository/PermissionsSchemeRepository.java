package org.snomed.cis.repository;

import org.snomed.cis.domain.PermissionsScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.List;

public interface PermissionsSchemeRepository extends JpaRepository<PermissionsScheme, Long> {

    List<PermissionsScheme> findByScheme(String scheme);

    List<PermissionsScheme> findByUsername(String username);

    @Modifying
    @Transactional
    void deleteBySchemeAndUsername(String scheme, String username);

    long countByUsernameIn(List<String> usernames);

}
