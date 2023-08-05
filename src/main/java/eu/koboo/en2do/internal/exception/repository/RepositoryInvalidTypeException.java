package eu.koboo.en2do.internal.exception.repository;

public class RepositoryInvalidTypeException extends Exception {

    public RepositoryInvalidTypeException(Class<?> typeClass, Class<?> asyncTypeClass, Class<?> repositoryClass) {
        super("The types of the Repository and the AsyncRepository " +
            "in the class \"" + repositoryClass.getName() + "\" " +
            "are not matching! " +
            "The incompatible types are first type \"" + typeClass.getName() + "\" " +
            "and second type \"" + asyncTypeClass.getName() + "\" " +
            "That's an error, please correct the generic types of this class.");
    }
}
