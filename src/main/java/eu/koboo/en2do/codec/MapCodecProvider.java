package eu.koboo.en2do.codec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;
import org.bson.json.JsonReader;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapCodecProvider implements PropertyCodecProvider {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            return new GenericMapCodec(type.getType(), registry.get(type.getTypeParameters().get(0)), registry.get(type.getTypeParameters().get(1)));
        }
        return null;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class GenericMapCodec<K, T> implements Codec<Map<K, T>> {

        private static final Logger LOGGER = Logger.getLogger(GenericMapCodec.class.getName());

        @Getter
        Class<Map<K, T>> encoderClass;
        Codec<K> keyCodec;
        Codec<T> valueCodec;

        GenericMapCodec(Class<Map<K, T>> encoderClass,
                        Codec<K> keyCodec, Codec<T> valueCodec,
                        Map<Class<?>, Class<? extends PropertyEditor>> map) {
            this.encoderClass = encoderClass;
            this.keyCodec = keyCodec;
            this.valueCodec = valueCodec;

            map.forEach(PropertyEditorManager::registerEditor);
        }

        GenericMapCodec(Class<Map<K, T>> encoderClass, Codec<K> keyCodec, Codec<T> valueCodec) {
            this.encoderClass = encoderClass;
            this.keyCodec = keyCodec;
            this.valueCodec = valueCodec;
        }

        @Override
        public void encode(BsonWriter writer, Map<K, T> map, EncoderContext encoderContext) {
            try (BsonDocumentWriter documentWriter = new BsonDocumentWriter(new BsonDocument())) {
                documentWriter.writeStartDocument();
                writer.writeStartDocument();

                for (Map.Entry<K, T> entry : map.entrySet()) {
                    PropertyEditor editor = PropertyEditorManager.findEditor(keyCodec.getEncoderClass());
                    if (editor != null) {
                        LOGGER.fine("Found PropertyEditor for class: " + keyCodec.getEncoderClass().getName());

                        editor.setValue(entry.getKey());
                        writer.writeName(editor.getAsText());
                    } else {
                        String documentId = UUID.randomUUID().toString();
                        documentWriter.writeName(documentId);
                        keyCodec.encode(documentWriter, entry.getKey(), encoderContext);
                        writer.writeName(documentWriter.getDocument().asDocument().get(documentId).asString().getValue());
                    }

                    valueCodec.encode(writer, entry.getValue(), encoderContext);
                }
                documentWriter.writeEndDocument();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to encode map: " + map, e);
                throw new IllegalArgumentException(e);
            }
            writer.writeEndDocument();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, T> decode(BsonReader reader, DecoderContext context) {
            reader.readStartDocument();
            Map<K, T> map = getInstance();
            while (!BsonType.END_OF_DOCUMENT.equals(reader.readBsonType())) {
                K key;
                PropertyEditor editor = PropertyEditorManager.findEditor(keyCodec.getEncoderClass());
                if (editor != null) {
                    LOGGER.fine("Found PropertyEditor for class: " + keyCodec.getEncoderClass().getName());
                    editor.setAsText(reader.readName());
                    key = (K) editor.getValue();
                } else {
                    JsonReader jsonReader = new JsonReader(String.format("\"key\": \"%s\"", reader.readName()));
                    key = keyCodec.decode(jsonReader, context);
                }

                T value = null;
                if (!BsonType.NULL.equals(reader.getCurrentBsonType())) {
                    value = valueCodec.decode(reader, context);
                }
                map.put(key, value);
            }
            reader.readEndDocument();
            return map;
        }

        private Map<K, T> getInstance() {
            if (encoderClass.isInterface()) {
                return new HashMap<>();
            }
            try {
                return encoderClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new CodecConfigurationException(e.getMessage(), e);
            }
        }
    }
}
