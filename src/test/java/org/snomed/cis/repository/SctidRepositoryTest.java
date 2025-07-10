package org.snomed.cis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
public abstract class SctidRepositoryTest implements SctidRepository {

    @Autowired
    private SctidRepository sctidRepository;

    public void updateJobId(List<String> ids, int jobId) {
        // No-op for testing
    }
}