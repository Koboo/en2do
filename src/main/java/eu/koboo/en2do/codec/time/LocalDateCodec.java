package eu.koboo.en2do.codec.time;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateCodec implements Codec<LocalDate> {

    @Override
    public void encode(BsonWriter writer, LocalDate value, EncoderContext encoderContext) {
        writer.writeDateTime(value.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    @Override
    public LocalDate decode(BsonReader reader, DecoderContext decoderContext) {
        Instant instant = Instant.ofEpochMilli(reader.readDateTime());
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
    }

    @Override
    public Class<LocalDate> getEncoderClass() {
        return LocalDate.class;
    }
}
