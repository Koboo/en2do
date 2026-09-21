package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.EntityMappingFactory;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;

/**
 * Base convention which resolves the shared mapping for the codec model type.
 */
abstract class EntityMappingConvention implements Convention {

    @Override
    public final void apply(ClassModelBuilder<?> classModelBuilder) {
        EntityMapping<?> entityMapping = EntityMappingFactory.create(classModelBuilder.getType());
        apply(classModelBuilder, entityMapping);
    }

    protected abstract void apply(ClassModelBuilder<?> classModelBuilder, EntityMapping<?> entityMapping);
}
