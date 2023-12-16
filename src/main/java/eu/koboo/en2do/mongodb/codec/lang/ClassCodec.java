package eu.koboo.en2do.mongodb.codec.lang;

import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * ClassCodec is used to encode and decode java.lang.Class objects to mongodb document fields.
 */
@SuppressWarnings("rawtypes")
public class ClassCodec implements Codec<Class> {

    /**
     * @param writer         the BSON writer to encode into
     * @param value          the value to encode
     * @param encoderContext the encoder context
     * @see org.bson.codecs.Encoder
     */
    @Override
    public void encode(BsonWriter writer, Class value, EncoderContext encoderContext) {
        writer.writeString(value.getName());
    }

    /**
     * @param reader         the BSON reader
     * @param decoderContext the decoder context
     * @return the decoded Class
     * @see org.bson.codecs.Decoder
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
     * @return the class of the encoded class
     * @see org.bson.codecs.Encoder
     */
    @Override
    public Class<Class> getEncoderClass() {
        return Class.class;
    }
}
