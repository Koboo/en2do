package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeConstructorException extends RepositoryTypeException {

    public RepositoryTypeConstructorException(Class<?> typeClass, Class<?> repositoryClass) {
        super("Couldn't find public no-args-constructor!\n" +
                "That constructor is important for the mongodb pojo-codec!",
            repositoryClass, typeClass);
    }
}
