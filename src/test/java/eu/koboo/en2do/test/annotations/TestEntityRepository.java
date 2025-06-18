package eu.koboo.en2do.test.annotations;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.UUID;

@Collection
public interface TestEntityRepository extends Repository<TestEntity, UUID> {


}
