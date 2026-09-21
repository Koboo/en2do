package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Removes method-only properties unless they are explicitly enabled.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class MethodMappingConvention extends EntityMappingConvention {

    boolean enableMethodProperties;

    @Override
    protected void apply(ClassModelBuilder<?> classModelBuilder, EntityMapping<?> entityMapping) {
        if (enableMethodProperties) {
            return;
        }

        Set<PropertyModelBuilder<?>> removableProperties = new LinkedHashSet<>();
        for (PropertyModelBuilder<?> property : classModelBuilder.getPropertyModelBuilders()) {
            if (entityMapping.findByJavaName(property.getName()) != null) {
                continue;
            }
            removableProperties.add(property);
        }
        for (PropertyModelBuilder<?> property : removableProperties) {
            classModelBuilder.removeProperty(property.getName());
        }
    }
}
