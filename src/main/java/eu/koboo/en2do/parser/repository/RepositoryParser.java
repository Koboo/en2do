package eu.koboo.en2do.parser.repository;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.parser.repository.indices.CompoundIndicesParser;
import eu.koboo.en2do.parser.repository.indices.GeoIndicesParser;
import eu.koboo.en2do.parser.repository.indices.IndicesParser;
import eu.koboo.en2do.parser.repository.indices.TimeToLiveIndicesParser;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.utility.parse.ParseUtils;
import eu.koboo.en2do.utility.reflection.FieldUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepositoryParser {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    SettingsBuilder builder;
    Map<Class<?>, Set<Field>> reflectedFieldIndex;
    Set<IndicesParser> indicesParserRegistry;

    public RepositoryParser(SettingsBuilder builder) {
        this.builder = builder;
        this.reflectedFieldIndex = new ConcurrentHashMap<>();
        this.indicesParserRegistry = new LinkedHashSet<>();

        this.indicesParserRegistry.add(new CompoundIndicesParser());
        this.indicesParserRegistry.add(new TimeToLiveIndicesParser());
        this.indicesParserRegistry.add(new GeoIndicesParser());
    }

    public void destroy() {
        for (Set<Field> value : reflectedFieldIndex.values()) {
            value.clear();
        }
        reflectedFieldIndex.clear();
        indicesParserRegistry.clear();
    }

    private Collection findCollectionAnnotation(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            Collection collectionAnnotation = clazz.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                continue;
            }
            return collectionAnnotation;
        }
        return null;
    }

    public Set<Field> parseEntityFields(Class<?> entityClass) {
        Set<Field> entityFieldSet = reflectedFieldIndex.get(entityClass);
        if (entityFieldSet != null) {
            return entityFieldSet;
        }
        entityFieldSet = FieldUtils.collectFields(entityClass);
        reflectedFieldIndex.put(entityClass, entityFieldSet);
        return entityFieldSet;
    }

    public Field parseEntityIdField(Class<?> entityClass) {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);
        // Get the field of the uniqueId of the entity.
        for (Field field : entityFieldSet) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            // Check for @Id annotation to find unique identifier of entity
            if (!field.isAnnotationPresent(Id.class)) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        return null;
    }

    public Map<String, Field> parseSortedFieldBsonNames(Class<?> entityClass) {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);

        // Create list of all entity fields with their
        // respective bson names and related them to the Field object.
        List<String> fieldBsonList = new LinkedList<>();
        Map<String, Field> unsortedFieldMap = new HashMap<>();
        for (Field field : entityFieldSet) {
            String bsonName = ParseUtils.parseBsonName(field);
            fieldBsonList.add(bsonName);
            unsortedFieldMap.put(bsonName, field);
        }

        // Sorting the list by length and putting them back into a linked map.
        fieldBsonList.sort(Comparator.comparingInt(String::length));
        Collections.reverse(fieldBsonList);
        Map<String, Field> sortedFieldMap = new LinkedHashMap<>();
        for (String bsonName : fieldBsonList) {
            sortedFieldMap.put(bsonName, unsortedFieldMap.get(bsonName));
        }

        fieldBsonList.clear();
        unsortedFieldMap.clear();

        return sortedFieldMap;
    }

    public void parseIndices(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> collection)
        throws Exception {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);
        for (IndicesParser indicesParser : indicesParserRegistry) {
            indicesParser.parse(repositoryClass, entityClass, collection, entityFieldSet);
        }
    }
}
