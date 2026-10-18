package eu.koboo.en2do.mongodb.indexparser;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;
import eu.koboo.en2do.mongodb.indexparser.indices.CompoundIndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.GeoIndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.IndicesParser;
import eu.koboo.en2do.mongodb.indexparser.indices.TimeToLiveIndicesParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class IndexParser {

    Set<IndicesParser> indicesParserRegistry;

    public IndexParser() {
        this.indicesParserRegistry = new LinkedHashSet<>();
        this.indicesParserRegistry.add(new CompoundIndicesParser());
        this.indicesParserRegistry.add(new TimeToLiveIndicesParser());
        this.indicesParserRegistry.add(new GeoIndicesParser());
    }

    public void close() {
        indicesParserRegistry.clear();
    }

    public void parseIndices(RepositoryMapping mapping, MongoCollection<?> collection) {
        for (IndicesParser indicesParser : indicesParserRegistry) {
            indicesParser.parse(mapping, collection);
        }
    }
}
