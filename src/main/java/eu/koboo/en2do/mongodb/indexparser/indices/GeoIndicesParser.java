package eu.koboo.en2do.mongodb.indexparser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Geometry;
import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import eu.koboo.en2do.repository.entity.compound.GeoIndex;
import org.bson.conversions.Bson;

public final class GeoIndicesParser implements IndicesParser {

    @Override
    public void parse(Class<?> repositoryClass, EntityMapping<?> entityMapping, MongoCollection<?> entityCollection) {
        for (FieldMapping field : entityMapping.getFieldsByJavaName().values()) {
            if (!Geometry.class.isAssignableFrom(field.getType())) {
                continue;
            }
            GeoIndex geoIndex = field.getAnnotation(GeoIndex.class);
            if (geoIndex == null) {
                continue;
            }
            String bsonName = field.getBsonName();
            Bson indexBson;
            if (geoIndex.sphere()) {
                indexBson = Indexes.geo2dsphere(bsonName);
            } else {
                indexBson = Indexes.geo2d(bsonName);
            }
            entityCollection.createIndex(indexBson);
        }
    }
}
