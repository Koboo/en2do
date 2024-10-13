package eu.koboo.en2do.parser.indices;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTTLFieldNotFoundException;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
import eu.koboo.en2do.utility.AnnotationUtils;
import eu.koboo.en2do.utility.GenericUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;

public class TimeToLiveIndicesParser implements IndicesParser {

    @Override
    public void parse(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> entityCollection,
                      Set<Field> entityFieldSet) throws Exception {
        Set<TTLIndex> ttlIndexSet = AnnotationUtils.collectAnnotations(entityClass, TTLIndex.class);
        for (TTLIndex ttlIndex : ttlIndexSet) {
            // Checking if the field in the annotation exists in the entity class.
            String ttlField = ttlIndex.value();
            boolean foundTTLField = false;
            for (Field entityField : entityFieldSet) {
                if (!entityField.getName().equalsIgnoreCase(ttlField)) {
                    continue;
                }
                if (GenericUtils.isNotTypeOf(entityField.getType(), Date.class)) {
                    continue;
                }
                foundTTLField = true;
                break;
            }
            if (!foundTTLField) {
                throw new RepositoryTTLFieldNotFoundException(repositoryClass, ttlField);
            }
            IndexOptions indexOptions = new IndexOptions()
                .expireAfter(ttlIndex.ttl(), ttlIndex.time());
            entityCollection.createIndex(Indexes.ascending(ttlField), indexOptions);
        }
    }
}
