package eu.koboo.en2do.internal.exception.repository;

public class RepositoryBeanInfoNotFoundException extends Exception {

    public RepositoryBeanInfoNotFoundException(Class<?> typeClass, Class<?> repoClass, Throwable e) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() + " " +
                "doesn't have a BeanInfo for it's fields!", e);
    }
}
