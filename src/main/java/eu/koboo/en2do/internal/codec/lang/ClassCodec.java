package eu.koboo.en2do.internal.codec.lang;

import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * ClassCodec is used to encode and decode java.lang.Class objects to mongodb document fields
 */
@SuppressWarnings("rawtypes")
public class ClassCodec implements Codec<Class> {

    /**
     * See org.bson.codecs.Encoder
     * @param writer the BSON writer to encode into
     * @param value the value to encode
     * @param encoderContext the encoder context
     */
    @Override
    public void encode(BsonWriter writer, Class value, EncoderContext encoderContext) {
        writer.writeString(value.getName());
    }

    /**
     * See org.bson.codecs.Decoder
     * @param reader         the BSON reader
     * @param decoderContext the decoder context
     * @return the decoded Class
     */
    @Override
    public Class decode(BsonReader reader, DecoderContext decoderContext) {
        String className = reader.readString();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BsonInvalidOperationException("Cannot create class from name \"" + className + "\"", e);
        }
    }

    /**
     * See org.bson.codecs.Encoder
     * @return the class of the encoded class
     */
    @Override
    public Class<Class> getEncoderClass() {
        return Class.class;
    }
}
