package eu.koboo.en2do.parser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Geometry;
import eu.koboo.en2do.repository.entity.compound.GeoIndex;
import eu.koboo.en2do.utility.FieldUtils;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class GeoIndicesParser implements IndicesParser {

    @Override
    public void parse(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> entityCollection,
                      Set<Field> entityFieldSet) {
        for (Field field : entityFieldSet) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)
                || Modifier.isFinal(modifiers)
                || Modifier.isTransient(modifiers)) {
                continue;
            }

            if (!Geometry.class.isAssignableFrom(field.getType())) {
                continue;
            }
            GeoIndex geoIndex = field.getAnnotation(GeoIndex.class);
            if (geoIndex == null) {
                continue;
            }
            String fieldName = FieldUtils.parseBsonName(field);
            Bson indexBson;
            if (geoIndex.sphere()) {
                indexBson = Indexes.geo2dsphere(fieldName);
            } else {
                indexBson = Indexes.geo2d(fieldName);
            }
            entityCollection.createIndex(indexBson);
        }
    }
}
