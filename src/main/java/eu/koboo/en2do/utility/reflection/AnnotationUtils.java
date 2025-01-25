package eu.koboo.en2do.utility.reflection;

import eu.koboo.en2do.repository.methods.transform.EmbeddedField;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A utility class for everything related to annotations
 */
@UtilityClass
@SuppressWarnings("unused")
public class AnnotationUtils {

    /**
     * This method is ued to get all annotations from an entity class, using while loop,
     * to iterate through all super-types.
     *
     * @param entityClass     The class, which should be scanned.
     * @param annotationClass the searched annotation
     * @param <E>             The generic type of the Class
     * @param <A>             The generic type of the annotation Class
     * @return The Set with all found annotations of type A
     */
    public <E, A extends Annotation> Set<A> collectAnnotations(Class<E> entityClass,
                                                               Class<A> annotationClass) {
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

    public Set<EmbeddedField> getEmbeddedFieldsSet(Method method) {
        EmbeddedField[] annotationsByType = method.getAnnotationsByType(EmbeddedField.class);
        return new LinkedHashSet<>(Arrays.asList(annotationsByType));
    }
}
