package eu.koboo.en2do.parser.indices;

import com.mongodb.client.MongoCollection;

import java.lang.reflect.Field;
import java.util.Set;

public interface IndicesParser {

    void parse(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> entityCollection,
               Set<Field> entityFieldSet) throws Exception;
}
