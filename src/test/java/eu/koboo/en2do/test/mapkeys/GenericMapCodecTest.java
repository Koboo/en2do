package eu.koboo.en2do.test.mapkeys;

import eu.koboo.en2do.mongodb.codec.types.GenericMapCodec;
import org.bson.*;
import org.bson.codecs.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericMapCodecTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testUuidKeysWithDocumentValues() {
        GenericMapCodec<UUID, BsonDocument> codec = new GenericMapCodec<>(
            (Class<Map<UUID, BsonDocument>>) (Class<?>) Map.class,
            new UuidCodec(UuidRepresentation.STANDARD), new BsonDocumentCodec());
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        BsonDocument firstEntry = new BsonDocument("online", BsonBoolean.TRUE);
        BsonDocument secondEntry = new BsonDocument("online", BsonBoolean.FALSE);
        Map<UUID, BsonDocument> expected = Map.of(firstId, firstEntry, secondId, secondEntry);

        BsonDocument document = new BsonDocument();
        try (BsonDocumentWriter writer = new BsonDocumentWriter(document)) {
            codec.encode(writer, expected, EncoderContext.builder().build());
        }
        assertEquals(new BsonDocument(firstId.toString(), firstEntry)
            .append(secondId.toString(), secondEntry), document);

        try (BsonDocumentReader reader = new BsonDocumentReader(document)) {
            assertEquals(expected, codec.decode(reader, DecoderContext.builder().build()));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUuidKeysWithStringValues() {
        GenericMapCodec<UUID, String> codec = new GenericMapCodec<>(
            (Class<Map<UUID, String>>) (Class<?>) Map.class,
            new UuidCodec(UuidRepresentation.STANDARD), new StringCodec());
        UUID id = UUID.randomUUID();
        BsonDocument document = new BsonDocument(id.toString(), new BsonString("friend"));

        try (BsonDocumentReader reader = new BsonDocumentReader(document)) {
            assertEquals(Map.of(id, "friend"), codec.decode(reader, DecoderContext.builder().build()));
        }
    }
}
