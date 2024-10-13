package eu.koboo.en2do.parser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryIndexFieldNotFoundException;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.utility.AnnotationUtils;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompoundIndicesParser implements IndicesParser {

    @Override
    public void parse(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> entityCollection,
                      Set<Field> entityFieldSet) throws Exception {
        Set<CompoundIndex> compoundIndexSet = AnnotationUtils.collectAnnotations(entityClass, CompoundIndex.class);
        for (CompoundIndex compoundIndex : compoundIndexSet) {
            // Checking if the field in the annotation exists in the entity class.
            Index[] fieldIndexes = compoundIndex.value();
            for (Index fieldIndex : fieldIndexes) {
                if (entityFieldSet.stream().map(Field::getName).noneMatch(fieldName -> fieldIndex.value().equalsIgnoreCase(fieldName))) {
                    throw new RepositoryIndexFieldNotFoundException(repositoryClass, fieldIndex.value());
                }
            }
            // Validated all fields and creating the indexes on the collection.
            List<Bson> indexBsonList = new ArrayList<>();
            for (Index fieldIndex : fieldIndexes) {
                String fieldName = fieldIndex.value();
                Bson bsonIndex;
                if (fieldIndex.ascending()) {
                    bsonIndex = Indexes.ascending(fieldName);
                } else {
                    bsonIndex = Indexes.descending(fieldName);
                }
                indexBsonList.add(bsonIndex);
            }
            IndexOptions indexOptions = new IndexOptions()
                .unique(compoundIndex.uniqueIndex());
            entityCollection.createIndex(Indexes.compoundIndex(indexBsonList), indexOptions);
        }
    }
}
