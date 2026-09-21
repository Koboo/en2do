package eu.koboo.en2do.mongodb.indexparser.indices;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.mongodb.mapping.EntityMapping;

public interface IndicesParser {

    void parse(Class<?> repositoryClass, EntityMapping<?> entityMapping, MongoCollection<?> entityCollection);
}
