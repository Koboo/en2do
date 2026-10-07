package eu.koboo.en2do.factory.mapping;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class FieldMapping {

    Field field;
    Class<?> type;
    String javaName;
    String bsonName;

    public Type getGenericType() {
        return field.getGenericType();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return field.getAnnotation(annotationType);
    }
}
