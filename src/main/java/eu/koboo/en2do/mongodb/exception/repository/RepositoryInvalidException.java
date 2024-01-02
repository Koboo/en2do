package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.repository.Repository;

public class RepositoryInvalidException extends Exception {

    public RepositoryInvalidException(Class<?> repoClass) {
        super("The class of " + repoClass.getName() + " is not from type " + Repository.class.getName() + "!");
    }
}
