package eu.koboo.en2do.codec;

import eu.koboo.en2do.codec.lang.ClassCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

public class SimpleCodecProvider implements CodecProvider {

    Map<Class<?>, Codec<?>> codecMap;

    public SimpleCodecProvider() {
        codecMap = new HashMap<>();

        addCodec(new ClassCodec());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return (Codec<T>) codecMap.get(clazz);
    }

    public <T> void addCodec(Codec<T> codec) {
        codecMap.put(codec.getEncoderClass(), codec);
    }
}
