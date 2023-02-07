package eu.koboo.en2do.internal.exception.repository;

public class RepositoryNoFieldsException extends Exception {

    public RepositoryNoFieldsException(Class<?> typeClass, Class<?> repoClass) {
        super("Couldn't find any fields in class of type " + typeClass.getName() + " " +
              "used in repository " + repoClass.getName() + "!");
    }
}
