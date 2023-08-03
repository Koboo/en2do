package eu.koboo.en2do.internal.exception.repository;

public class RepositorySetterNotFoundException extends Exception {

    public RepositorySetterNotFoundException(Class<?> typeClass, Class<?> repoClass, String fieldName) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() + " " +
                "uses a invalid or no setter method for the field \"" + fieldName + "\"!");
    }
}
