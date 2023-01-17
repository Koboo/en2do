package eu.koboo.en2do.codec.time;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.LocalTime;

public class LocalTimeCodec implements Codec<LocalTime> {

    @Override
    public void encode(BsonWriter writer, LocalTime value, EncoderContext encoderContext) {
        writer.writeString(value.toString());
    }

    @Override
    public LocalTime decode(BsonReader reader, DecoderContext decoderContext) {
        return LocalTime.parse(reader.readString());
    }

    @Override
    public Class<LocalTime> getEncoderClass() {
        return LocalTime.class;
    }
}
