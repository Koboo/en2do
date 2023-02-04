package eu.koboo.en2do.repository;

import eu.koboo.en2do.repository.methods.async.Async;
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
     * @see Repository#countAll()
     * @return Future, with the count of all entities
     */
    @Async
    @NotNull
    CompletableFuture<Long> asyncCountAll();

    /**
     * Async representation
     * @see Repository#delete(Object)
     * @param entity The entity, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDelete(@NotNull E entity);

    /**
     * Async representation
     * @see Repository#deleteAll(List)
     * @param entityList The List with entities, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDeleteAll(@NotNull List<E> entityList);

    /**
     * Async representation
     * @see Repository#deleteById(Object)
     * @param identifier The identifier of the entity, which should be deleted
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDeleteById(@NotNull ID identifier);

    /**
     * Async representation
     * @see Repository#drop()
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncDrop();

    /**
     * Async representation
     * @see Repository#exists(Object)
     * @param entity The entity, which should be checked
     * @return Future, with a boolean, which indicates if the entity exists
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncExists(@NotNull E entity);

    /**
     * Async representation
     * @see Repository#existsById(Object)
     * @param identifier The identifier of the entity, which should be checked
     * @return Future, with a boolean, which indicates if an entity with the id exists
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncExistsById(@NotNull ID identifier);

    /**
     * Async representation
     * @see Repository#findAll()
     * @return Future, with all entities
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncFindAll();

    /**
     * Async representation
     * @see Repository#findFirstById(Object)
     * @param identifier The identifier of the entity, which should be found
     * @return Future, with the first entity with the id
     */
    @Async
    @NotNull
    CompletableFuture<E> asyncFindFirstById(@NotNull ID identifier);

    /**
     * Async representation
     * @see Repository#pageAll(Pagination)
     * @param pagination The options, which should be used for pagination
     * @return Future, with all entities, paged by the Pagination object
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncPageAll(@NotNull Pagination pagination);

    /**
     * Async representation
     * @see Repository#save(Object)
     * @param entity The entity, which should be saved
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncSave(@NotNull E entity);

    /**
     * Async representation
     * @see Repository#saveAll(List)
     * @param entityList The List of entities, which should be saved
     * @return Future, with a boolean of success
     */
    @Async
    @NotNull
    CompletableFuture<Boolean> asyncSaveAll(@NotNull List<E> entityList);

    /**
     * Async representation
     * @see Repository#sortAll(Sort)
     * @param sort The options, which should be used for sorting
     * @return Future, with all entities, sorted by the Sort object
     */
    @Async
    @NotNull
    CompletableFuture<List<E>> asyncSortAll(@NotNull Sort sort);
}
