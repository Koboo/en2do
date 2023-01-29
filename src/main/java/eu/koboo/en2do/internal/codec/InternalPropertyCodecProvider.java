package eu.koboo.en2do.internal.codec;

import eu.koboo.en2do.internal.codec.lang.ClassCodec;
import eu.koboo.en2do.internal.codec.map.GenericMapCodec;
import lombok.extern.java.Log;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

/**
 * This codec provider enables the usage of the en2do custom codecs and adds them to the CodecRegistry
 */
@Log
public class InternalPropertyCodecProvider implements PropertyCodecProvider {

    /**
     * @see PropertyCodecProvider
     * @param type the class and bound type parameters for which to get a Codec
     * @param registry the registry to use for resolving dependent Codec instances
     * @return The codec from the type
     * @param <T> The type of the codec
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        if (Class.class.isAssignableFrom(type.getType())) {
            return (Codec<T>) new ClassCodec();
        }
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            return new GenericMapCodec(type.getType(), registry.get(type.getTypeParameters().get(0)),
                    registry.get(type.getTypeParameters().get(1)));
        }
        return null;
    }
}
