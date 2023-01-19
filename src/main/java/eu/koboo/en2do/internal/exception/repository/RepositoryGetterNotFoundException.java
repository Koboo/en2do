package eu.koboo.en2do.internal.exception.repository;

public class RepositoryGetterNotFoundException extends Exception {

    public RepositoryGetterNotFoundException(Class<?> typeClass, Class<?> repoClass, String fieldName) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() +
                " doesn't have a getter method for the field \"" + fieldName + "\"!");
    }
}
