package eu.koboo.en2do.exception;

import eu.koboo.en2do.index.Id;

public class RepositoryIdNotFoundException extends Exception {

    public RepositoryIdNotFoundException(Class<?> repoClass) {
        super("Couldn't find " + Id.class.getName() + " field in entity of " + repoClass.getName() + "! That's a required annotation.");
    }
}
