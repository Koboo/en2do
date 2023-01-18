package eu.koboo.en2do.exception;

public class RepositoryEntityConstructorException extends Exception {

    public RepositoryEntityConstructorException(Class<?> entityClass, Class<?> repoClass) {
        super("The Entity class " + entityClass.getName() + " of repository " + repoClass.getName() +
                " doesn't have a valid public no-args-constructor! The constructor is important for the mongodb-pojo-codec!");
    }
}
