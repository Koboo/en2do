package eu.koboo.en2do.repository.exception;

public class RepositoryIdNotFoundException extends Exception {

    public RepositoryIdNotFoundException(Class<?> repoClass) {
        super("Couldn't the @Id field in entity of" + repoClass.getName() + "! That's a required annotation.");
    }
}