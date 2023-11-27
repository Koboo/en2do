package eu.koboo.en2do.mongodb.codec;

import eu.koboo.en2do.mongodb.codec.lang.ClassCodec;
import eu.koboo.en2do.mongodb.codec.map.GenericMapCodec;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This codec provider enables the usage of the en2do custom codecs and adds them to the CodecRegistry.
 */
@Log
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalPropertyCodecProvider implements PropertyCodecProvider {

    Map<Class<?>, Codec<?>> customCodecRegistry;

    public InternalPropertyCodecProvider() {
        this.customCodecRegistry = new LinkedHashMap<>();
    }

    public <T> void registerCodec(Class<T> typeClass, Codec<T> typeCodec) {
        this.customCodecRegistry.put(typeClass, typeCodec);
    }

    /**
     * @param type     the class and bound type parameters for which to get a Codec
     * @param registry the registry to use for resolving dependent Codec instances
     * @param <T>      The type of the codec
     * @return The codec from the type
     * @see PropertyCodecProvider
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        Class<T> typeClass = type.getType();
        List<? extends TypeWithTypeParameters<?>> typeParameters = type.getTypeParameters();

        if (Map.class.isAssignableFrom(typeClass) && typeParameters.size() == 2) {
            return new GenericMapCodec(typeClass, registry.get(typeParameters.get(0)),
                registry.get(typeParameters.get(1)));
        }
        if (Class.class.isAssignableFrom(typeClass)) {
            return (Codec<T>) new ClassCodec();
        }
        Codec<?> codec = customCodecRegistry.get(typeClass);
        if(codec != null) {
            return (Codec<T>) codec;
        }
        return null;
    }

}
