package eu.koboo.en2do.internal.exception.repository;

public class RepositoryConstructorException extends Exception {

    public RepositoryConstructorException(Class<?> entityClass, Class<?> repoClass) {
        super("The class " + entityClass.getName() + " used in repository " + repoClass.getName() + " " +
            "doesn't have a valid public no-args-constructor! " +
            "The constructor is important for the mongodb pojo-codec!");
    }
}
