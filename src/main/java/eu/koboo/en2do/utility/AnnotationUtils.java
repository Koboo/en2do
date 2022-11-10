package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class AnnotationUtils {

    public <E, A extends Annotation> Set<A> collectAnnotations(Class<E> entityClass, Class<A> annotationClass) {
        Set<A> annotationSet = new HashSet<>();
        Class<?> clazz = entityClass;
        while (clazz != Object.class) {
            A[] indexArray = clazz.getAnnotationsByType(annotationClass);
            clazz = clazz.getSuperclass();
            if (indexArray.length == 0) {
                continue;
            }
            annotationSet.addAll(Arrays.asList(indexArray));
        }
        return annotationSet;
    }
}
