package eu.koboo.en2do.internal.exception.repository;

public class RepositoryDescriptorException extends Exception {

    public RepositoryDescriptorException(Class<?> typeClass, Class<?> repoClass, String descriptor) {
        super("The class " + typeClass.getName() + " used in repository " + repoClass.getName() +
                " doesn't have a field for descriptor \"" + descriptor + "\"!");
    }
}
