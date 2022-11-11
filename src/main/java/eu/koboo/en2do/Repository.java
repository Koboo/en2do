package eu.koboo.en2do;

import java.util.List;

public interface Repository<E, ID> {

    String getCollectionName();

    ID getUniqueId(E entity);

    Class<E> getEntityClass();

    Class<ID> getEntityUniqueIdClass();

    E findFirstById(ID identifier);

    List<E> findMany();

    boolean delete(E entity);

    boolean deleteById(ID identifier);

    boolean deleteAll(List<E> entityList);

    boolean drop();

    boolean save(E entity);

    boolean saveAll(List<E> entityList);

    boolean exists(E entity);

    boolean existsById(ID identifier);
}
