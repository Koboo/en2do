package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A utility class for everything related to entity properties.
 */
@UtilityClass
@SuppressWarnings("unused")
public class EntityUtils {

    /**
     * This method is used to copy all field values from one entity to another.
     * It also works with inheritance.
     *
     * @param from The entity to copy from
     * @param to   The entity to copy to
     */
    public void copyProperties(Object from, Object to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();

        try {
            BeanInfo fromBean = Introspector.getBeanInfo(fromClass);
            BeanInfo toBean = Introspector.getBeanInfo(toClass);
            PropertyDescriptor[] toPropDecArray = toBean.getPropertyDescriptors();
            List<PropertyDescriptor> fromPropDescList = List.of(fromBean.getPropertyDescriptors());
            for (PropertyDescriptor toPropDesc : toPropDecArray) {
                int fromPropDescIndex = fromPropDescList.indexOf(toPropDesc);
                if (fromPropDescIndex == -1) {
                    continue;
                }
                PropertyDescriptor fromPropDesc = fromPropDescList.get(fromPropDescIndex);
                if (!fromPropDesc.getDisplayName().equals(toPropDesc.getDisplayName())) {
                    continue;
                }
                if (fromPropDesc.getDisplayName().equals("class")) {
                    continue;
                }
                Method writeMethod = toPropDesc.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                Method readMethod = fromPropDesc.getReadMethod();
                if (readMethod == null) {
                    continue;
                }
                writeMethod.invoke(to, readMethod.invoke(from));
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException |
                 IllegalArgumentException e) {
            throw new RuntimeException("Couldn't copy clone properties. ", e);
        }
    }

    /**
     * This method is used to get all annotations from an entity class, using while loop,
     * to iterate through all super-types.
     *
     * @param entityClass     The class, which should be scanned.
     * @param annotationClass the searched annotation
     * @param <A>             The generic type of the annotation Class
     * @return The Set with all found annotations of type A
     */
    public <A extends Annotation> Set<A> collectAnnotations(Class<?> entityClass,
                                                            Class<A> annotationClass) {
        Set<A> annotations = new HashSet<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            annotations.addAll(List.of(current.getAnnotationsByType(annotationClass)));
            current = current.getSuperclass();
        }
        return annotations;
    }
}
