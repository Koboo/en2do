package eu.koboo.en2do.repository;

import eu.koboo.en2do.repository.options.methods.paging.Pager;
import eu.koboo.en2do.repository.options.methods.sort.Sort;

import java.util.List;

@SuppressWarnings("unused")
public interface Repository<E, ID> {

    long countAll();

    boolean delete(E entity);

    boolean deleteAll(List<E> entityList);

    boolean deleteById(ID identifier);

    boolean drop();

    boolean exists(E entity);

    boolean existsById(ID identifier);

    List<E> findAll();

    E findFirstById(ID identifier);

    String getCollectionName();

    Class<E> getEntityClass();

    Class<ID> getEntityUniqueIdClass();

    ID getUniqueId(E entity);

    boolean save(E entity);

    boolean saveAll(List<E> entityList);

    List<E> sortAll(Sort sort);
}
