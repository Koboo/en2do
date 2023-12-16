package eu.koboo.en2do.mongodb.exception.repository;

import java.util.Date;

public class RepositoryTTLFieldNotFoundException extends Exception {

    public RepositoryTTLFieldNotFoundException(Class<?> repoClass, String indexName) {
        super("Couldn't find " + indexName + " field for time-to-live index in entity of " + repoClass.getName() + ". " +
            "To set a ttl index the field has to be type " + Date.class.getName() + ".");
    }
}
