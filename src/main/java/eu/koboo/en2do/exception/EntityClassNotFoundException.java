package eu.koboo.en2do.exception;

public class EntityClassNotFoundException extends Exception {

    public EntityClassNotFoundException(Class<?> repoClass, Throwable cause) {
        super("The Entity class of repository " + repoClass.getName() + " could not be found!", cause);
    }

}