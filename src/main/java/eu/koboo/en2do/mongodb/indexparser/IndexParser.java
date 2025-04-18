package eu.koboo.en2do.mongodb.indexparser;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.mongodb.indexparser.indices.CompoundIndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.GeoIndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.IndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.TimeToLiveIndicesParser;
import eu.koboo.en2do.utility.reflection.FieldUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexParser {

    Set<IndicesParser> indicesParserRegistry;

    public IndexParser() {
        this.indicesParserRegistry = new LinkedHashSet<>();
        this.indicesParserRegistry.add(new CompoundIndicesParser());
        this.indicesParserRegistry.add(new TimeToLiveIndicesParser());
        this.indicesParserRegistry.add(new GeoIndicesParser());
    }

    public void destroy() {
        indicesParserRegistry.clear();
    }

    public void parseIndices(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> collection) {
        Set<Field> entityFieldSet = FieldUtils.collectFields(entityClass);
        for (IndicesParser indicesParser : indicesParserRegistry) {
            indicesParser.parse(repositoryClass, entityClass, collection, entityFieldSet);
        }
    }
}
