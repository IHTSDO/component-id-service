package com.snomed.api.repository;

import com.snomed.api.domain.PermissionsScheme;
import com.snomed.api.domain.PermissionsSchemePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PermissionsSchemeRepository extends JpaRepository<PermissionsScheme, Long> {
    List<PermissionsScheme> findByScheme(String scheme);

    public List<PermissionsScheme> findByUsername(String username);
@Modifying
@Transactional
    void deleteBySchemeAndUsername(String scheme,String username);

}
