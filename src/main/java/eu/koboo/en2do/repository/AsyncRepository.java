package eu.koboo.en2do.repository;

import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Sort;

import java.util.Collection;
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
     *
     * @return Future, with the count of all entities
     * @see Repository#countAll()
     */
    @Async
    CompletableFuture<Long> asyncCountAll();

    /**
     * Async representation
     *
     * @param entity The entity, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#delete(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncDelete(E entity);

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#deleteById(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteById(ID identifier);

    /**
     * Async representation
     *
     * @param entityList The List with entities, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#deleteMany(Collection)
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteMany(Collection<E> entityList);

    /**
     * Async representation
     *
     * @param idList The List with ids of the entities, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#deleteManyById(Collection)
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteManyById(Collection<ID> idList);

    /**
     * Async representation
     *
     * @return Future, with a boolean of success
     * @see Repository#drop()
     */
    @Async
    CompletableFuture<Boolean> asyncDrop();

    /**
     * Async representation
     *
     * @param entity The entity, which should be checked
     * @return Future, with a boolean, which indicates if the entity exists
     * @see Repository#exists(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncExists(E entity);

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be checked
     * @return Future, with a boolean, which indicates if an entity with the id exists
     * @see Repository#existsById(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncExistsById(ID identifier);

    /**
     * Async representation
     *
     * @return Future, with all entities
     * @see Repository#findAll()
     */
    @Async
    CompletableFuture<List<E>> asyncFindAll();

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be found
     * @return Future, with the first entity with the id
     * @see Repository#findFirstById(Object)
     */
    @Async
    CompletableFuture<E> asyncFindFirstById(ID identifier);

    /**
     * Async representation
     *
     * @param pagination The options, which should be used for pagination
     * @return Future, with all entities, paged by the Pagination object
     * @see Repository#pageAll(Pagination)
     */
    @Async
    CompletableFuture<List<E>> asyncPageAll(Pagination pagination);

    /**
     * Async representation
     *
     * @param entity The entity, which should be saved
     * @return Future, with a boolean of success
     * @see Repository#save(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncSave(E entity);

    /**
     * Async representation
     *
     * @param entityList The List of entities, which should be saved
     * @return Future, with a boolean of success
     * @see Repository#saveAll(Collection)
     */
    @Async
    CompletableFuture<Boolean> asyncSaveAll(Collection<E> entityList);

    /**
     * Async representation
     *
     * @param sort The options, which should be used for sorting
     * @return Future, with all entities, sorted by the Sort object
     * @see Repository#sortAll(Sort)
     */
    @Async
    CompletableFuture<List<E>> asyncSortAll(Sort sort);

    /**
     * Async representation
     *
     * @param updateBatch The UpdateBatch to use.
     * @return true, if the operation was successful.
     * @see Repository#updateAllFields(UpdateBatch)
     */
    @Async
    CompletableFuture<Boolean> asyncUpdateAllFields(UpdateBatch updateBatch);
}
