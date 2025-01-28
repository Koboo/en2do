package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeBeanInfoException extends RepositoryTypeException {

    public RepositoryTypeBeanInfoException(Class<?> typeClass, Class<?> repositoryClass, Throwable e) {
        super("Couldn't find BeanInfo of typeClass!", typeClass, repositoryClass, e);
    }
}
