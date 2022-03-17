package com.snomed.api.repository;

import com.snomed.api.domain.PermissionsScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionsSchemeRepository extends JpaRepository<PermissionsScheme, Long> {
    List<PermissionsScheme> findByScheme(String scheme);
}
