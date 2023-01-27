package eu.koboo.en2do.internal.convention;

import eu.koboo.en2do.internal.RepositoryMeta;
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
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AnnotationConvention implements Convention {

    Map<Class<?>, RepositoryMeta<?, ?, ?>> repositoryMetaRegistry;

    private RepositoryMeta<?, ?, ?> findRepositoryMetaOf(Class<?> typeClass) {
        for (RepositoryMeta<?, ?, ?> meta : repositoryMetaRegistry.values()) {
            if (!meta.getEntityClass().equals(typeClass)) {
                continue;
            }
            return meta;
        }
        return null;
    }

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        RepositoryMeta<?, ?, ?> repositoryMeta = findRepositoryMetaOf(classModelBuilder.getType());
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            for (Annotation readAnnotation : propertyModelBuilder.getReadAnnotations()) {
                if (readAnnotation instanceof Transient) {
                    propertyModelBuilder.readName(null);
                    continue;
                }
                if (readAnnotation instanceof TransformField transformField) {
                    propertyModelBuilder.readName(transformField.value());
                    continue;
                }
                if (repositoryMeta != null && readAnnotation instanceof Id && repositoryMeta.isOverrideObjectId()) {
                    classModelBuilder.idPropertyName(propertyModelBuilder.getName());
                }
            }
            for (Annotation writeAnnotation : propertyModelBuilder.getWriteAnnotations()) {
                if (writeAnnotation instanceof Transient) {
                    propertyModelBuilder.writeName(null);
                    continue;
                }
                if (writeAnnotation instanceof TransformField transformField) {
                    propertyModelBuilder.writeName(transformField.value());
                }
            }
        }
    }
}
