package eu.koboo.en2do.mongodb.exception.repository;

public class RepositoryInvalidGetterException extends Exception {

    public RepositoryInvalidGetterException(Class<?> typeClass, Class<?> repoClass, String fieldName) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() +
            " doesn't have a valid getter method for the field \"" + fieldName + "\"! " +
            "It's needs to be public and match the parameter count of 0.");
    }
}
