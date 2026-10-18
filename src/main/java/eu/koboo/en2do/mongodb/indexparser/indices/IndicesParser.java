package eu.koboo.en2do.mongodb.indexparser.indices;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;

public interface IndicesParser {

    void parse(RepositoryMapping mapping, MongoCollection<?> entityCollection);
}
