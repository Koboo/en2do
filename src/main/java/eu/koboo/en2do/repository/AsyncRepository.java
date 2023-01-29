package eu.koboo.en2do.repository;

import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Sort;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The default Repository interface, which predefines several useful methods.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/methods/predefined-methods">...</a>
 * See documentation: <a href="https://koboo.gitbook.io/en2do/get-started/create-the-repository">...</a>
 *
 * @param <E>  The generic type of the Entity
 * @param <ID> The generic type of the field annotated with "@Id" in the Entity
 */
@SuppressWarnings("unused")
public interface AsyncRepository<E, ID> {

    /**
     * Async representation
     * @see Repository#countAll()
     * @return Future, with the count of all entities
     */
    @Async
    CompletableFuture<Long> asyncCountAll();

    /**
     * Async representation
     * @see Repository#delete(Object)
     * @param entity The entity, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncDelete(E entity);

    /**
     * Async representation
     * @see Repository#deleteAll(List)
     * @param entityList The List with entities, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteAll(List<E> entityList);

    /**
     * Async representation
     * @see Repository#deleteById(Object)
     * @param identifier The identifier of the entity, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteById(ID identifier);

    /**
     * Async representation
     * @see Repository#drop()
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncDrop();

    /**
     * Async representation
     * @see Repository#exists(Object)
     * @param entity The entity, which should be checked
     * @return Future, with a boolean, which indicates if the entity exists
     */
    @Async
    CompletableFuture<Boolean> asyncExists(E entity);

    /**
     * Async representation
     * @see Repository#existsById(Object)
     * @param identifier The identifier of the entity, which should be checked
     * @return Future, with a boolean, which indicates if an entity with the id exists
     */
    @Async
    CompletableFuture<Boolean> asyncExistsById(ID identifier);

    /**
     * Async representation
     * @see Repository#findAll()
     * @return Future, with all entities
     */
    @Async
    CompletableFuture<List<E>> asyncFindAll();

    /**
     * Async representation
     * @see Repository#findFirstById(Object)
     * @param identifier The identifier of the entity, which should be found
     * @return Future, with the first entity with the id
     */
    @Async
    CompletableFuture<E> asyncFindFirstById(ID identifier);

    /**
     * Async representation
     * @see Repository#pageAll(Pagination)
     * @param pagination The options, which should be used for pagination
     * @return Future, with all entities, paged by the Pagination object
     */
    @Async
    CompletableFuture<List<E>> asyncPageAll(Pagination pagination);

    /**
     * Async representation
     * @see Repository#save(Object)
     * @param entity The entity, which should be saved
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncSave(E entity);

    /**
     * Async representation
     * @see Repository#saveAll(List)
     * @param entityList The List of entities, which should be saved
     * @return Future, with a boolean of success
     */
    @Async
    CompletableFuture<Boolean> asyncSaveAll(List<E> entityList);

    /**
     * Async representation
     * @see Repository#sortAll(Sort)
     * @param sort The options, which should be used for sorting
     * @return Future, with all entities, sorted by the Sort object
     */
    @Async
    CompletableFuture<List<E>> asyncSortAll(Sort sort);
}
