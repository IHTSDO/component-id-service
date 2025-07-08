package org.snomed.cis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public abstract class SctidRepositoryTest implements SctidRepository {

    @Autowired
    private SctidRepository sctidRepository;


}