package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeIndexCompoundException extends RepositoryTypeException {

    public RepositoryTypeIndexCompoundException(Class<?> repositoryClass, Class<?> typeClass, String indexName) {
        super("Couldn't find indexed field with name: \"" + indexName + "\". Please check the correct name of the field.",
            repositoryClass, typeClass);
    }
}
