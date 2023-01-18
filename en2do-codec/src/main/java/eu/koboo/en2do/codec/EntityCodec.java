package eu.koboo.en2do.codec;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class EntityCodec<E> implements CollectibleCodec<E> {

    @Override
    public E generateIdIfAbsentFromDocument(E document) {
        return null;
    }

    @Override
    public boolean documentHasId(E document) {
        return false;
    }

    @Override
    public BsonValue getDocumentId(E document) {
        return null;
    }

    @Override
    public E decode(BsonReader reader, DecoderContext decoderContext) {
        return null;
    }

    @Override
    public void encode(BsonWriter writer, E value, EncoderContext encoderContext) {

    }

    @Override
    public Class<E> getEncoderClass() {
        return null;
    }
}
