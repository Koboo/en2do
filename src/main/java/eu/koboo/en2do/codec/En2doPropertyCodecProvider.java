package eu.koboo.en2do.codec;

import eu.koboo.en2do.codec.lang.ClassCodec;
import eu.koboo.en2do.codec.map.GenericMapCodec;
import lombok.extern.java.Log;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

@Log
public class En2doPropertyCodecProvider implements PropertyCodecProvider {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        if(Class.class.isAssignableFrom(type.getType())) {
            return (Codec<T>) new ClassCodec();
        }
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            return new GenericMapCodec(type.getType(), registry.get(type.getTypeParameters().get(0)),
                    registry.get(type.getTypeParameters().get(1)));
        }
        return null;
    }
}
