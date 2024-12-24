package eu.koboo.en2do.mongodb.convention;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.parser.RepositoryParser;
import eu.koboo.en2do.utility.FieldUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This convention implementation disables the saving of methods
 * which start with "get*" or "set*". MongoDB Pojo codec thinks that these methods
 * are property read or write methods, but most of the time you don't want to save them.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MethodMappingConvention implements Convention {

    MongoManager mongoManager;
    RepositoryParser parser;

    /**
     * @param classModelBuilder the ClassModelBuilder to apply the convention to
     * @see Convention
     */
    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        // If the setting is enabled, we don't need to remove the method properties
        // from the class model builder.
        if (mongoManager.getSettingsBuilder().isEnableMethodProperties()) {
            return;
        }
        Class<?> entityClass = classModelBuilder.getType();
        Set<Field> fieldSet = parser.parseEntityFields(entityClass);
        Set<String> nonFieldProperties = new LinkedHashSet<>();
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            String propertyName = propertyModelBuilder.getName();
            Field field = FieldUtils.findFieldByName(propertyName, fieldSet);
            if (field != null) {
                continue;
            }
            nonFieldProperties.add(propertyName);
        }
        for (String nonFieldProperty : nonFieldProperties) {
            classModelBuilder.removeProperty(nonFieldProperty);
        }
    }
}
