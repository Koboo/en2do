package eu.koboo.en2do.codec.time;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {

    @Override
    public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext) {
        writer.writeDateTime(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    @Override
    public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext) {
        Instant instant = Instant.ofEpochMilli(reader.readDateTime());
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Override
    public Class<LocalDateTime> getEncoderClass() {
        return LocalDateTime.class;
    }
}
