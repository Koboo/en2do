package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.repository.entity.TransformField;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;

public final class TransformFieldConvention implements Convention {

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> modelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            for (Annotation readAnnotation : modelBuilder.getReadAnnotations()) {
                if(!(readAnnotation instanceof TransformField transformField)) {
                    continue;
                }
                modelBuilder.readName(transformField.value());
                break;
            }
            for (Annotation writeAnnotation : modelBuilder.getWriteAnnotations()) {
                if(!(writeAnnotation instanceof TransformField transformField)) {
                    continue;
                }
                modelBuilder.writeName(transformField.value());
                break;
            }
        }
    }
}
