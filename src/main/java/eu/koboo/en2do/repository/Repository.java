package eu.koboo.en2do.repository;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Sort;

import java.util.Collection;
import java.util.List;

/**
 * The default Repository interface, which predefines several useful methods.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/methods/predefined-methods">...</a>
 * See documentation: <a href="https://koboo.gitbook.io/en2do/get-started/create-the-repository">...</a>
 *
 * @param <E>  The generic type of the Entity
 * @param <ID> The generic type of the field annotated with "@Id" in the Entity
 */
@SuppressWarnings("unused")
public interface Repository<E, ID> {

    /**
     * This method counts all documents of the collection in the mongodb.
     *
     * @return The amount of total entities in this repository.
     */
    long countAll();

    /**
     * This method deletes the given entity, by filtering with the entity's "@Id" field/unique identifier.
     *
     * @param entity The entity, which should be deleted.
     * @return true, if the entity was deleted successfully.
     */
    boolean delete(E entity);

    /**
     * This method deletes all entities of the repository.
     * Difference between drop is, that the indices stay the same.
     *
     * @return true, if entities were successfully deleted.
     */
    boolean deleteAll();

    /**
     * This method deletes the entity with the given identifier, filtering like the "#delete(E entity)" method.
     *
     * @param identifier The unique identifier of the entity, which should be deleted.
     * @return true, if the entity was deleted successfully.
     */
    boolean deleteById(ID identifier);

    /**
     * This method deletes all entities of the given list, filtering like the "#delete(E entity)" method.
     *
     * @param entityList The List with the entities, which should be deleted.
     * @return true, if all entities were deleted successfully.
     */
    boolean deleteMany(Collection<E> entityList);

    /**
     * This method deletes all entities with the id within the given list.
     *
     * @param idList The List with the ids of the entities, which should be deleted.
     * @return true, if all entities were deleted successfully.
     */
    boolean deleteManyById(Collection<ID> idList);

    /**
     * Drops / deletes all entities of the repository.
     *
     * @return true, if all entities were deleted successfully.
     */
    boolean drop();

    /**
     * Checks if the given entity exists in the repository, by filtering with the entity's
     * "@Id" field/unique identifier.
     *
     * @param entity The entity, which should be checked.
     * @return true, if the entity exists in the collection.
     */
    boolean exists(E entity);

    /**
     * Checks if an entity with the given unique identifier exists in the repository, like the "#exists(E entity)" method.
     *
     * @param identifier The identifier of the entity, which should be checked.
     * @return true, if an entity with the given identifier exists in the collection.
     */
    boolean existsById(ID identifier);

    /**
     * Finds all entities of the collection
     *
     * @return A List with all entities of the repository.
     */
    List<E> findAll();

    /**
     * Find the first entity with the given unique identifier.
     * If the entity is not found, "null" is returned.
     *
     * @param identifier The unique identifier of the entity, which is used to filter.
     * @return The found entity, if it exists, or "null" if it not exists.
     */

    E findFirstById(ID identifier);

    /**
     * @return The collection name, defined by the "@Collection" annotation of the repository.
     */
    String getCollectionName();

    /**
     * @return The Class of the entity of the repository.
     */
    Class<E> getEntityClass();

    /**
     * @return The Class of the unique identifier of the entity of the repository.
     */
    Class<ID> getEntityUniqueIdClass();

    /**
     * This method is used to get the unique identifier of the given entity.
     * If the entity doesn't have a unique identifier, a NullPointerException is thrown.
     *
     * @param entity The entity, which unique identifier, should be returned
     * @return The unique identifier of the given entity.
     */
    ID getUniqueId(E entity);

    /**
     * This method is used to set the unique identifier of the given entity.
     *
     * @param entity The entity, on which the unique identifier should be set
     */
    void setUniqueId(E entity, ID uniqueId);

    /**
     * This method applies the pagination of all entities of the repository.
     *
     * @param pagination The pagination, which is used to page the entities.
     * @return A List with the paged entities.
     */
    List<E> pageAll(Pagination pagination);

    /**
     * Saves the given entity to the database.
     * If the entity exists, the existing document is updated.
     * If the entity doesn't exist, a new document is created.
     *
     * @param entity The entity, which should be saved.
     * @return true, if the entity was successfully saved.
     */
    boolean save(E entity);

    /**
     * Saves all entities of the given List to the database.
     *
     * @param entityList A List of the entities, which should be saved
     * @return true, if the entities were successfully saved.
     */
    boolean saveAll(Collection<E> entityList);

    /**
     * This method applies the Sort object of all entities of the repository.
     *
     * @param sort The Sort object, which should be used to sort all entities.
     * @return A List with the sorted entities.
     */
    List<E> sortAll(Sort sort);

    /**
     * This method uses the UpdateBatch object to update the fields of all documents.
     *
     * @param updateBatch The UpdateBatch to use.
     * @return true, if the operation was successful.
     */
    boolean updateAllFields(UpdateBatch updateBatch);

    /**
     * Allows access to the native mongodb collection,
     * for more advanced queries or en2do-unsupported mongodb stuff.
     *
     * @return the native mongodb collection object
     */
    MongoCollection<E> getNativeCollection();
}
