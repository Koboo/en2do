package eu.koboo.en2do.mongodb.codec.types;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class StringConversionCodec<T> implements Codec<T> {

    Class<T> type;
    Function<T, String> toString;
    Function<String, T> fromString;

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext context) {
        writer.writeString(toString.apply(value));
    }

    @Override
    public T decode(BsonReader reader, DecoderContext context) {
        return fromString.apply(reader.readString());
    }

    @Override
    public Class<T> getEncoderClass() {
        return type;
    }
}