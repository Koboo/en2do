package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.repository.entity.NonIndex;
import eu.koboo.en2do.repository.options.SeparateEntityId;

public class RepositoryNonIndexIdException extends Exception {

    public RepositoryNonIndexIdException(Class<?> repoClass) {
        super("The repository " + repoClass.getName() + " uses " + NonIndex.class + ", but is missing " +
            SeparateEntityId.class + ". This is not allowed, because the ObjectId of mongodb has to be unique!");
    }
}
