package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes method-only properties unless they are explicitly enabled.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class MethodMappingConvention implements Convention {

    MongoManager mongoManager;

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        if (mongoManager.getMongoSettings().enableMethodProperties()) {
            return;
        }
        // We are searching for fields with the given property name.
        // If we can't find a field, we remove the property from the database-mapping.
        List<String> removableProperties = new ArrayList<>();
        for (PropertyModelBuilder<?> property : classModelBuilder.getPropertyModelBuilders()) {
            String name = property.getName();
            boolean fieldFound = false;

            for (Class<?> clazz = classModelBuilder.getType(); clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    clazz.getDeclaredField(name);
                    fieldFound = true;
                    break;
                } catch (NoSuchFieldException ignored) {
                    // Not a field of the type. Skip.
                }
            }

            if (!fieldFound) {
                removableProperties.add(name);
            }
        }

        for (String name : removableProperties) {
            classModelBuilder.removeProperty(name);
        }
    }
}
