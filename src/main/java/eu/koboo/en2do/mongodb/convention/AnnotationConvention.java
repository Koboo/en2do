package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;

/**
 * This convention implementation enables the usage of the annotations from en2do
 * inside entity classes. This convention checks the annotations in the class model builder
 * and modifies it accordingly.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AnnotationConvention implements Convention {

    /**
     * @param classModelBuilder the ClassModelBuilder to apply the convention to
     * @see Convention
     */
    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            for (Annotation readAnnotation : propertyModelBuilder.getReadAnnotations()) {
                if (readAnnotation instanceof Transient) {
                    propertyModelBuilder.readName(null);
                    continue;
                }
                if (readAnnotation instanceof TransformField) {
                    TransformField transformField = (TransformField) readAnnotation;
                    propertyModelBuilder.readName(transformField.value());
                    continue;
                }
                if (readAnnotation instanceof Id) {
                    classModelBuilder.idPropertyName(propertyModelBuilder.getName());
                }
            }
            for (Annotation writeAnnotation : propertyModelBuilder.getWriteAnnotations()) {
                if (writeAnnotation instanceof Transient) {
                    propertyModelBuilder.writeName(null);
                    continue;
                }
                if (writeAnnotation instanceof TransformField) {
                    TransformField transformField = (TransformField) writeAnnotation;
                    propertyModelBuilder.writeName(transformField.value());
                }
                if (writeAnnotation instanceof Id) {
                    classModelBuilder.idPropertyName(propertyModelBuilder.getName());
                }
            }
        }
    }
}
