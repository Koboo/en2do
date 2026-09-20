package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeIdMismatchException extends RepositoryTypeException {

    public RepositoryTypeIdMismatchException(Class<?> repositoryClass, Class<?> entityClass,
                                             Class<?> declaredIdClass, Class<?> fieldIdClass) {
        super("Repository ID type " + declaredIdClass.getName()
            + " does not match the entity ID type " + fieldIdClass.getName() + ".",
            repositoryClass, entityClass);
    }
}
