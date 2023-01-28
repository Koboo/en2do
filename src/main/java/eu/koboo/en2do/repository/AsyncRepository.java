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
     */
    @Async
    CompletableFuture<Long> asyncCountAll();

    /**
     * Async representation
     * @see Repository#delete(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncDelete(E entity);

    /**
     * Async representation
     * @see Repository#deleteAll(List)
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteAll(List<E> entityList);

    /**
     * Async representation
     * @see Repository#deleteById(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncDeleteById(ID identifier);

    /**
     * Async representation
     * @see Repository#drop()
     */
    @Async
    CompletableFuture<Boolean> asyncDrop();

    /**
     * Async representation
     * @see Repository#exists(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncExists(E entity);

    /**
     * Async representation
     * @see Repository#existsById(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncExistsById(ID identifier);

    /**
     * Async representation
     * @see Repository#findAll()
     */
    @Async
    CompletableFuture<List<E>> asyncFindAll();

    /**
     * Async representation
     * @see Repository#findFirstById(Object)
     */
    @Async
    CompletableFuture<E> asyncFindFirstById(ID identifier);

    /**
     * Async representation
     * @see Repository#pageAll(Pagination)
     */
    @Async
    CompletableFuture<List<E>> asyncPageAll(Pagination pagination);

    /**
     * Async representation
     * @see Repository#save(Object)
     */
    @Async
    CompletableFuture<Boolean> asyncSave(E entity);

    /**
     * Async representation
     * @see Repository#saveAll(List)
     */
    @Async
    CompletableFuture<Boolean> asyncSaveAll(List<E> entityList);

    /**
     * Async representation
     * @see Repository#sortAll(Sort)
     */
    @Async
    CompletableFuture<List<E>> asyncSortAll(Sort sort);
}
