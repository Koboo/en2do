package eu.koboo.en2do.internal.exception;

public class RepositoryEntityNotFoundException extends Exception {

    public RepositoryEntityNotFoundException(Class<?> repoClass, Throwable cause) {
        super("The Entity class of " + repoClass.getName() + " could not be found!", cause);
    }
}
