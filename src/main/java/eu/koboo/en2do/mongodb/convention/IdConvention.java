package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.repository.entity.Id;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the mapped entity identifier to the MongoDB codec model.
 */
public final class IdConvention implements Convention {

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> modelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            List<Annotation> annotationList = new ArrayList<>(modelBuilder.getReadAnnotations());
            annotationList.addAll(modelBuilder.getWriteAnnotations());
            if (annotationList.stream().noneMatch(Id.class::isInstance)) {
                continue;
            }
            classModelBuilder.idPropertyName(modelBuilder.getName());
            break;
        }
    }
}
