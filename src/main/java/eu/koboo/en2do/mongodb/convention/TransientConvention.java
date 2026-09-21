package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.repository.entity.Transient;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Removes properties marked as transient from MongoDB codec models.
 */
public final class TransientConvention implements Convention {

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        Set<PropertyModelBuilder<?>> properties = new LinkedHashSet<>(
            classModelBuilder.getPropertyModelBuilders());
        for (PropertyModelBuilder<?> property : properties) {
            if (hasTransientAnnotation(property.getReadAnnotations())
                || hasTransientAnnotation(property.getWriteAnnotations())) {
                classModelBuilder.removeProperty(property.getName());
            }
        }
    }

    private boolean hasTransientAnnotation(Iterable<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Transient) {
                return true;
            }
        }
        return false;
    }
}
