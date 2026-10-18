package eu.koboo.en2do.mongodb.indexparser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.factory.mapping.FieldMapping;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIndexCompoundException;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.utility.EntityUtils;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CompoundIndicesParser implements IndicesParser {

    @Override
    public void parse(RepositoryMapping mapping, MongoCollection<?> entityCollection) {
        Class<?> repositoryClass = mapping.getRepositoryClass();
        Class<?> entityClass = mapping.getEntityClass();
        Set<CompoundIndex> compoundIndexSet = EntityUtils.collectAnnotations(entityClass, CompoundIndex.class);
        for (CompoundIndex compoundIndex : compoundIndexSet) {
            List<Bson> indexBsonList = new ArrayList<>();
            for (Index fieldIndex : compoundIndex.value()) {
                FieldMapping field = mapping.findByJavaName(fieldIndex.value());
                if (field == null) {
                    throw new RepositoryTypeIndexCompoundException(repositoryClass, entityClass, fieldIndex.value());
                }
                String bsonName = field.getBsonName();
                Bson bsonIndex;
                if (fieldIndex.ascending()) {
                    bsonIndex = Indexes.ascending(bsonName);
                } else {
                    bsonIndex = Indexes.descending(bsonName);
                }
                indexBsonList.add(bsonIndex);
            }
            IndexOptions indexOptions = new IndexOptions()
                .unique(compoundIndex.uniqueIndex());
            entityCollection.createIndex(Indexes.compoundIndex(indexBsonList), indexOptions);
        }
    }
}
