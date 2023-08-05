package eu.koboo.en2do.internal.exception.repository;

public class RepositoryInvalidSetterException extends Exception {

    public RepositoryInvalidSetterException(Class<?> typeClass, Class<?> repoClass, String fieldName) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() +
            " doesn't have a valid setter method for the field \"" + fieldName + "\"! " +
            "It's needs to be public and match the parameter count of 1.");
    }
}
