package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

import java.lang.reflect.Field;

public class RepositoryTypeFieldDuplicatedException extends RepositoryTypeException {

    public RepositoryTypeFieldDuplicatedException(Field field, Class<?> repositoryClass, Class<?> typeClass) {
        super("Duplicated fields found with name \"" + field.getName() + "\".", repositoryClass, typeClass);
    }
}
