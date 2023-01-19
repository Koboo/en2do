package eu.koboo.en2do.internal.convention;

import eu.koboo.en2do.repository.entity.Transient;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;

public class TransientConvention implements Convention {

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            for (Annotation readAnnotation : propertyModelBuilder.getReadAnnotations()) {
                if(readAnnotation instanceof Transient) {
                    propertyModelBuilder.readName(null);
                }
            }
            for (Annotation writeAnnotation : propertyModelBuilder.getWriteAnnotations()) {
                if(writeAnnotation instanceof Transient) {
                    propertyModelBuilder.writeName(null);
                }
            }
        }
    }
}
