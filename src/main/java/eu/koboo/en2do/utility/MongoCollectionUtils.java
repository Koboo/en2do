package eu.koboo.en2do.utility;

import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.repository.Collection;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MongoCollectionUtils {

    public String createCollectionName(SettingsBuilder builder, String entityCollectionName) {

        String collectionPrefix = builder.getCollectionPrefix();
        if (collectionPrefix != null) {
            entityCollectionName = collectionPrefix + entityCollectionName;
        }

        String collectionSuffix = builder.getCollectionSuffix();
        if (collectionSuffix != null) {
            entityCollectionName = entityCollectionName + collectionSuffix;
        }

        return entityCollectionName;
    }

    public Collection parseAnnotation(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            Collection collectionAnnotation = clazz.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                continue;
            }
            return collectionAnnotation;
        }
        return null;
    }
}
