package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import org.bson.codecs.pojo.ClassModelBuilder;

/**
 * Applies the mapped entity identifier to the MongoDB codec model.
 */
public final class IdConvention extends EntityMappingConvention {

    @Override
    protected void apply(ClassModelBuilder<?> classModelBuilder, EntityMapping<?> entityMapping) {
        FieldMapping idField = entityMapping.getIdField();
        if (idField == null) {
            return;
        }
        classModelBuilder.idPropertyName(idField.getJavaName());
    }
}
