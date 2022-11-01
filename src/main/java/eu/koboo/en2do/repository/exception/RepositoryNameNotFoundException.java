package eu.koboo.en2do.repository.exception;

import eu.koboo.en2do.Collection;

public class RepositoryNameNotFoundException extends Exception {

    public RepositoryNameNotFoundException(Class<?> repoClass) {
        super("No or invalid collection name given through " + Collection.class + " annotation in " + repoClass.getName() + "! " +
                "That's a required annotation.");
    }
}
