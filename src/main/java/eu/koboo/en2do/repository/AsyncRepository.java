package eu.koboo.en2do.repository;

import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Sort;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    CompletableFuture<Long> asyncCountAll();

    /**
     * Async representation
     *
     * @param entity The entity, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#delete(Object)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDelete(@NotNull E entity);

    /**
     * Async representation
     *
     * @param entityList The List with entities, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#deleteAll(List)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDeleteAll(@NotNull List<E> entityList);

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be deleted
     * @return Future, with a boolean of success
     * @see Repository#deleteById(Object)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDeleteById(@NotNull ID identifier);

    /**
     * Async representation
     *
     * @return Future, with a boolean of success
     * @see Repository#drop()
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDrop();

    /**
     * Async representation
     *
     * @param entity The entity, which should be checked
     * @return Future, with a boolean, which indicates if the entity exists
     * @see Repository#exists(Object)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncExists(@NotNull E entity);

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be checked
     * @return Future, with a boolean, which indicates if an entity with the id exists
     * @see Repository#existsById(Object)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncExistsById(@NotNull ID identifier);

    /**
     * Async representation
     *
     * @return Future, with all entities
     * @see Repository#findAll()
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncFindAll();

    /**
     * Async representation
     *
     * @param identifier The identifier of the entity, which should be found
     * @return Future, with the first entity with the id
     * @see Repository#findFirstById(Object)
     */
    @Async
    @NotNull
    CompletableFuture<E> asyncFindFirstById(@NotNull ID identifier);

    /**
     * Async representation
     *
     * @param pagination The options, which should be used for pagination
     * @return Future, with all entities, paged by the Pagination object
     * @see Repository#pageAll(Pagination)
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncPageAll(@NotNull Pagination pagination);

    /**
     * Async representation
     *
     * @param entity The entity, which should be saved
     * @return Future, with a boolean of success
     * @see Repository#save(Object)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncSave(@NotNull E entity);

    /**
     * Async representation
     *
     * @param entityList The List of entities, which should be saved
     * @return Future, with a boolean of success
     * @see Repository#saveAll(List)
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncSaveAll(@NotNull List<E> entityList);

    /**
     * Async representation
     *
     * @param sort The options, which should be used for sorting
     * @return Future, with all entities, sorted by the Sort object
     * @see Repository#sortAll(Sort)
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncSortAll(@NotNull Sort sort);

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
