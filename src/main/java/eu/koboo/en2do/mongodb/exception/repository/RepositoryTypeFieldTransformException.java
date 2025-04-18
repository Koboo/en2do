package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeFieldTransformException extends RepositoryTypeException {

    public RepositoryTypeFieldTransformException(Class<?> typeClass, Class<?> repositoryClass, String fieldName) {
        super("The given typeClass has an invalid name for the field \"" + fieldName + "\" in @TransformField annotation!",
            repositoryClass, typeClass);
    }
}
