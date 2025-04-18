package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeIndexTTLException extends RepositoryTypeException {

    public RepositoryTypeIndexTTLException(Class<?> repositoryClass, Class<?> typeClass, String indexName) {
        super("Couldn't find \"" + indexName + "\" field for time-to-live index. " +
                "To set a ttl index the field has to be from type \"java.util.Date\".",
            repositoryClass, typeClass);
    }
}
