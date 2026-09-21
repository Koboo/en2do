package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PropertyModelBuilder;

/**
 * Applies resolved BSON field names to MongoDB codec properties.
 */
public final class TransformFieldConvention extends EntityMappingConvention {

    @Override
    protected void apply(ClassModelBuilder<?> classModelBuilder, EntityMapping<?> entityMapping) {
        for (PropertyModelBuilder<?> property : classModelBuilder.getPropertyModelBuilders()) {
            FieldMapping field = entityMapping.findByJavaName(property.getName());
            if (field == null || field == entityMapping.getIdField()) {
                continue;
            }
            String bsonName = field.getBsonName();
            if (bsonName.equals(field.getJavaName())) {
                continue;
            }
            property.readName(bsonName);
            property.writeName(bsonName);
        }
    }
}
