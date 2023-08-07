package eu.koboo.en2do.mongodb.exception.repository;

public class RepositoryEntityNotFoundException extends Exception {

    public RepositoryEntityNotFoundException(Class<?> repoClass) {
        super("The class of the entity of repository " + repoClass.getName() + " could not be found!");
    }
}
