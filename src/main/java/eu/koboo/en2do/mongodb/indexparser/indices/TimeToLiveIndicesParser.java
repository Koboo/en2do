package eu.koboo.en2do.mongodb.indexparser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIndexTTLException;
import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
import eu.koboo.en2do.utility.parse.ParseUtils;

import java.util.Date;
import java.util.Set;

public final class TimeToLiveIndicesParser implements IndicesParser {

    @Override
    public void parse(Class<?> repositoryClass, EntityMapping<?> entityMapping, MongoCollection<?> entityCollection) {
        Class<?> entityClass = entityMapping.getEntityClass();
        Set<TTLIndex> ttlIndexSet = ParseUtils.getAllAnnotations(entityClass, TTLIndex.class);
        for (TTLIndex ttlIndex : ttlIndexSet) {
            String ttlField = ttlIndex.value();
            FieldMapping field = entityMapping.findByJavaNameIgnoreCase(ttlField);
            if (field == null || !Date.class.isAssignableFrom(field.getType())) {
                throw new RepositoryTypeIndexTTLException(repositoryClass, entityClass, ttlField);
            }
            IndexOptions indexOptions = new IndexOptions()
                .expireAfter(ttlIndex.ttl(), ttlIndex.time());
            entityCollection.createIndex(Indexes.ascending(field.getBsonName()), indexOptions);
        }
    }
}
