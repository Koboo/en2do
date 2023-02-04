package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class for everything related to entity properties.
 */
@UtilityClass
public class EntityUtils {

    /**
     * This method is used to copy all field values from one entity to another.
     * It also works with inheritance.
     * @param from The entity to copy from
     * @param to   The entity to copy to
     */
    public void copyProperties(@NotNull Object from, @NotNull Object to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();

        try {
            BeanInfo fromBean = Introspector.getBeanInfo(fromClass);
            BeanInfo toBean = Introspector.getBeanInfo(toClass);

            PropertyDescriptor[] toPropDecArray = toBean.getPropertyDescriptors();
            List<PropertyDescriptor> fromPropDescList = Arrays.asList(fromBean
                    .getPropertyDescriptors());

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
                if (toPropDesc.getWriteMethod() == null) {
                    continue;
                }
                toPropDesc.getWriteMethod().invoke(to, fromPropDesc.getReadMethod().invoke(from));
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException |
                 IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
