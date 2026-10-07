package eu.koboo.en2do.factory.mapping;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RepositoryMappingFactory {

    private static final ClassValue<RepositoryMapping> MAPPING_CACHE = new ClassValue<>() {
        @Override
        protected RepositoryMapping computeValue(@NonNull Class<?> repositoryClass) {
            return new RepositoryMapping(repositoryClass);
        }
    };

    public RepositoryMapping create(Class<?> repositoryClass) {
        if (repositoryClass == null) {
            throw new NullPointerException("repositoryClass is null");
        }
        return MAPPING_CACHE.get(repositoryClass);
    }
}
